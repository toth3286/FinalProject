/*	Jay Hennen & Chris Rouse
 * 	SuperBlock.java
 * 	CSS 430 ~ Professor Fukuda
 */
public class FileSystem {
	private int diskBlocks;
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	public FileSystem(int diskBlocks){							//Constructor
		this.diskBlocks = diskBlocks;							//Set this diskblocks to the parameter's value
		superblock = new SuperBlock(diskBlocks);				//Create a new Superblock
		directory = new Directory(superblock.totalInodes);		//Create a new directory with totalInodes's worth of values
		filetable = new FileTable(directory);					//Create a new Filetable and include the directory in it
		
		int dirEnt = SysLib.open("/","r");								//Open the file related to the root with read flagged
		int dirSize = SysLib.fsize(dirEnt);							//the directory size is set to the filesize of the directory entry
		if(dirSize > 0){										//If the size is greater than zero
			byte[] dirData = new byte[dirSize];					//Create a buffer
			read(dirEnt, dirData);								//Read the data
			directory.bytes2directory(dirData);					//read in the directory
		}
		SysLib.close(dirEnt);											//Close the directory entry
	}
	
	public int format(int files){								//Format the FileSystem, and by association, the superblock
		byte[] b = new byte[1000];								//Create a new disk buffer
		SysLib.int2bytes(diskBlocks, b, 0);						//Convert to bytes
		SysLib.rawwrite(0, b);									//Write the data to the disk
		superblock.format(files);								//Call the superblock format function
		return 0;
	}
	
	public FileTableEntry open(String fileName, String mode){				//Used to open a file
		short inode = directory.namei(fileName.substring(1));
		FileTableEntry ftEnt;
		if (inode > -1) { 													// file exists
			ftEnt = filetable.falloc(fileName, mode);						//Allocate a new file table entry with the name and the mode
			return ftEnt;
		} else {															//File does not exist
			if(mode.compareTo("w") == 0 || mode.compareTo("w+") == 0){
				
			}else if(mode.compareTo("r") == 0){
				
			}else if(mode.compareTo("a") == 0){
				
			}else{
				
			}
		}
		return ftEnt;														//Otherwise retutn the filetableentry
	}
	
	public synchronized int read(int fd, byte buffer[]){
		
	}
	
	public synchronized int write(int fd, byte buffer[]){
		
	}
	
	public synchronized int seek(FileTableEntry fd, int offset, int whence){
		if(whence < 1){											//if whence is less than one, we start from offset from the beginning of the file
			fd.seekPtr = 0 + offset;
		}else if(whence == 1){									//if whence is one, we start from the current pointer location plus offset
			fd.seekPtr = fd.seekPtr += offset;
		}else if(whence == 2){									//If set to two, we set to the size of the file plus the offset (can be positive/negative)
			fd.seekPtr = fd.inode.length + offset;
		}else{													//If greater than two, we set pointer to end of file
			fd.seekPtr = fd.inode.length;
		}
		if (fd.seekPtr > fd.inode.length)
			fd.seekPtr = fd.inode.length;
		if (fd.seekPtr < 0)
			fd.seekPtr = 0;
		return 0;
	}
	
	public synchronized int close(FileTableEntry fte){						//Closes the entry pertaining to fd, commits all file transations,
		fte.count--;
		if (fte.count == 0)
			filetable.ffree(fte);	//unregisters from fd table,
		return 0;
	}
	
	public synchronized int delete(String fileName){			//Delete the filename specified. Waits for transactions to finish first
																//New transactions will not be allowed while waiting to delete
	}
	
	public int fsize(int fd, TCB curr){							//Returns the size (in bytes) of the fd given
		return curr.ftEnt[fd].inode.length;
	}
	
	void sync(){

	}
	
	private void deallocateAllBlocks(Inode inode){				//Remove all blocks and reset the Inode
		int seekptr = inode.length;								//move the seek pointer to the end
		int blk = inode.findTargetBlock(seekptr);				//Find target seek block
		while (seekptr > 0) {									//while the seek pointer is larger than zero
			superblock.returnBlock(blk);						//return the block
			seekptr -= Disk.blockSize;							//decrement the seek pointer
		}
		if (inode.indirect != -1) {								//if Indirect has entries		
			superblock.returnBlock(inode.indirect);				//return each indirect entry
		}
		inode.count = 0;										//reset all variables in the end
		inode.length = 0;
		inode.flag = -1;
		for(int i = 0; i < inode.direct.length;i++){			//Reset all direct pointers
			inode.direct[i] = -1;
		}
	}
}
