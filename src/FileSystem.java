/*	Coded by: Jay Hennen & Chris Rouse
 * 	SuperBlock.java
 * 	CSS 430 ~ Professor Fukuda
 */
public class FileSystem {
	private int diskBlocks;
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;
	private int blocksize = 512;

	public FileSystem(int diskBlocks){							//Constructor
		this.diskBlocks = diskBlocks;							//Set this diskblocks to the parameter's value
		superblock = new SuperBlock(diskBlocks);				//Create a new Superblock
		directory = new Directory(superblock.totalInodes);		//Create a new directory with totalInodes's worth of values
		filetable = new FileTable(directory, superblock.totalInodes);//Create a new Filetable and include the directory in it
		
		int dirEnt = SysLib.open("/","r");						//Open the file related to the root with read flagged
		int dirSize = SysLib.fsize(dirEnt);						//the directory size is set to the filesize of the directory entry
		if(dirSize > 0){										//If the size is greater than zero
			byte[] dirData = new byte[dirSize];					//Create a buffer
			SysLib.read(dirEnt, dirData);						//Read the data
			directory.bytes2directory(dirData);					//read in the directory
		}
		SysLib.close(dirEnt);									//Close the directory entry
	}
	
	public int format(int files){								//Format the FileSystem, and by association, the superblock
		byte[] b = new byte[1000];								//Create a new disk buffer
		SysLib.int2bytes(diskBlocks, b, 0);						//Convert to bytes
		SysLib.rawwrite(0, b);									//Write the data to the disk
		superblock.format(files);								//Call the superblock format function
		return 0;
	}
	
	public synchronized FileTableEntry open(String fileName, String mode){//Used to open a file
		assert(!fileName.equals("/"));
		short inode = directory.namei(fileName);
		if (inode == -1 && mode.equals("r"))
			return null;
		FileTableEntry ftEnt;
		if (inode < 0) 											// file doesnt exist and have write permission
			directory.ialloc(fileName);
		ftEnt = filetable.falloc(fileName, mode);				//Allocate a new file table entry with the name and the mode

		if(mode.compareTo("w") == 0){
			deallocateAllBlocks(ftEnt.inode);
		} else if(mode.compareTo("a") == 0){
			ftEnt.seekPtr = ftEnt.inode.length;
		} else if (mode.compareTo("r") == 0 || mode.compareTo("w+")==0){
		} else {
			throw new IllegalArgumentException();
		}
		return ftEnt;											//Otherwise return the filetableentry
	}
	
	public int read(FileTableEntry f, byte[] buffer){
		synchronized(f.inode) {									//Synchronize the read block
			int bufptr = 0;
																// while buffer isn't full or seek pointer hasn't reached end of file
			while (bufptr != buffer.length && f.seekPtr != f.inode.length) {//Continue as long as bufptr doesn't reach buffer's length OR the pointer doesn't reach the inode's length
				byte[] inBlk = new byte[512];					//Create an array for the inBlocks
				int offset = f.seekPtr % Disk.blockSize;		//mod the pointer to the block
				int readLength = Math.min(Disk.blockSize-offset, buffer.length - bufptr);//get the minimum of the blocksize - offset/buffer length - offset. 
				int curBlk = f.inode.findTargetBlock(f.seekPtr);//Whichever min hits first is the condition to compare to
				SysLib.rawread(curBlk, inBlk);					//Find target block and then read it from the disk
				System.arraycopy(inBlk, offset, buffer, bufptr, readLength);//Copy the part read into the buffer
				bufptr += readLength;							//Keep incrementing the readlength accordingly
				f.seekPtr+= readLength;							//Adjust the pointer accordingly
			}
			return bufptr;										//Return a pointer to the number read
		}
	}
	
	public synchronized int write(FileTableEntry f, byte[] buffer){
		//Instead of adding blocks when necessary, allocate all blocks at start
		int extra = f.seekPtr + buffer.length - f.inode.length;
		while(extra > 0) {
			extra -= Disk.blockSize;
			addBlock(f.inode);
		}
		int bufptr = 0;
		do {													//As long as the pointer isn't at buffer's length, keep going
			byte outBlk[] = new byte[Disk.blockSize];			//Create the output block
			int offset = f.seekPtr%Disk.blockSize;				//Mod the offset to the block
			int writeLength = Math.min(Disk.blockSize-offset, buffer.length - bufptr);//Get the minimum of blocksize - offset/buffer's length - pointer
			int curBlk = f.inode.findTargetBlock(f.seekPtr);	//Whichever it hits first is what we want to compare to
			f.seekPtr+= writeLength;							//When we know min, find the target block, then increment the pointer accordingly
			if (f.seekPtr > f.inode.length)						//If the pointer is greater than the inode's length
				f.inode.length = f.seekPtr;						//set the inode length to the pointer
			SysLib.rawread(curBlk, outBlk);						//Read the data from the disk into the outblock
			System.arraycopy(buffer, bufptr, outBlk, offset, writeLength);//Copy arrays to outblock
			bufptr += writeLength;								//Move the pointer accordingly
			SysLib.rawwrite(curBlk, outBlk);					//Write the data
		}while(bufptr != buffer.length);
		return bufptr;											//When finished, return bytes written
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

		return fd.seekPtr;
	}
	
	public synchronized int close(FileTableEntry fte){			//Closes the entry pertaining to fd, commits all file transations,
		fte.count--;
		if (fte.count == 0)
			filetable.ffree(fte);								//unregisters from fd table,
		return 0;
	}
	
	public synchronized int delete(String filename){			//Delete the filename specified. Waits for transactions to finish first
		FileTableEntry f = open(filename, "w");
		directory.ifree(f.iNumber);
		close(f);
		return 0;
		//New transactions will not be allowed while waiting to delete
	}
	
	public int fsize(int fd, TCB curr){							//Returns the size (in bytes) of the fd given
		return curr.ftEnt[fd].inode.length;
	}
	
	void sync(){
		FileTableEntry f = open("/", "w");						//Open the root directory
		byte[] b = directory.directory2bytes();					//COnvert the directory to bytes as preprocessing
		write(f,b);												//Write the data to the disk
		close(f);												//Close root
		superblock.sync();										//Call superblock to continue the sync
		return;
	}
	
	private void deallocateAllBlocks(Inode inode){				//Remove all blocks and reset the Inode
		int seekptr = inode.length;								//move the seek pointer to the end
		int blk = inode.findTargetBlock(seekptr);				//Find target seek block
		while (seekptr > 0) {									//while the seek pointer is larger than zero
			blk = inode.findTargetBlock(seekptr);				//Find target seek block
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
	
	private int addBlock(Inode inode) {							//Used when we want to add another block
		int newBlk = superblock.getFreeBlock();					//Have the superblock hand us a free block
		int retVal = inode.addBlock(newBlk);					//Add the block to the inode
		if (retVal == -2) {										//If retVal is flagged for being full
			int newIndirectBlk = superblock.getFreeBlock();		//Allocate the indirect block
			inode.registerIndirectBlock(newIndirectBlk);		//Register the free block in the indirect block
			retVal = inode.addBlock(newBlk);					
		}
		return retVal;
	}	
}
