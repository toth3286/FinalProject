/*	Jay Hennen & Chris Rouse
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
		filetable = new FileTable(directory, superblock.totalInodes);					//Create a new Filetable and include the directory in it
		
		int dirEnt = SysLib.open("/","r");								//Open the file related to the root with read flagged
		int dirSize = SysLib.fsize(dirEnt);							//the directory size is set to the filesize of the directory entry
		if(dirSize > 0){										//If the size is greater than zero
			byte[] dirData = new byte[dirSize];					//Create a buffer
			SysLib.read(dirEnt, dirData);								//Read the data
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
	
	public synchronized FileTableEntry open(String fileName, String mode){				//Used to open a file
		assert(!fileName.equals("/"));
		short inode = directory.namei(fileName);
		FileTableEntry ftEnt;
		if (inode < 0) 							// file doesnt exist
			directory.ialloc(fileName);
		ftEnt = filetable.falloc(fileName, mode);						//Allocate a new file table entry with the name and the mode

		if(mode.compareTo("w") == 0 || mode.compareTo("w+") == 0){
			deallocateAllBlocks(ftEnt.inode);
		} else if(mode.compareTo("a") == 0){
			ftEnt.seekPtr = ftEnt.inode.length;
		} else if (mode.compareTo("r") == 0){
		} else {
			throw new IllegalArgumentException();
		}
		return ftEnt;														//Otherwise retutn the filetableentry
	}
	
	public int read(FileTableEntry f, byte[] buffer){
		synchronized(f.inode) {
		int bRead = 0;
		int filelength = f.inode.length;
		int bufflength = buffer.length;
		
		while(bufflength > 0 && f.seekPtr != f.inode.length){
			int blk = f.inode.findTargetBlock(f.seekPtr);
			byte[] newblk = new byte[blocksize];
			
			SysLib.rawread(blk, newblk);
			int tmpptr = f.seekPtr % blocksize;
			int leftToRead = blocksize - tmpptr;
			int fileresidual = filelength - f.seekPtr;
			
			if(bufflength > leftToRead){
				
				if(fileresidual > leftToRead){
					System.arraycopy(newblk, tmpptr, buffer, bRead, leftToRead);
					bRead += leftToRead;
					f.seekPtr += leftToRead;
					bufflength -= leftToRead;
				}else{
					System.arraycopy(newblk, tmpptr, buffer, bRead, fileresidual);
					bRead += fileresidual;
					f.seekPtr += fileresidual;
					bufflength -= bufflength;
				}
			}else{
				if(fileresidual > bufflength){
					System.arraycopy(newblk, tmpptr, buffer, bRead, bufflength);
					f.seekPtr += bufflength;
					bRead += bufflength;
					bufflength -= bufflength;
				}else{
					System.arraycopy(newblk, tmpptr, buffer, bRead, fileresidual);
					f.seekPtr += fileresidual;
					bRead += fileresidual;
					bufflength -= bufflength;
				}
			}
		}
		return bRead;
		}
	}
	
	public synchronized int write(FileTableEntry f, byte[] buffer){
		int bufptr = 0;
		System.err.println(f.seekPtr + "  " + f.inode.length);
		do {
			StringBuffer s = new StringBuffer(100);

			if(f.seekPtr == f.inode.length && f.seekPtr%Disk.blockSize == 0) {

				addBlock(f.inode);
			}
			byte outBlk[] = new byte[Disk.blockSize];
			int offset = f.seekPtr%Disk.blockSize;
			int writeLength = Math.min(Disk.blockSize-offset, buffer.length - bufptr);
			f.seekPtr+= writeLength;
			if (f.seekPtr > f.inode.length)
				f.inode.length += writeLength;
			int curBlk = f.inode.findTargetBlock(f.seekPtr);
			//System.err.println(curBlk);
			SysLib.rawread(curBlk, outBlk);
			System.arraycopy(buffer, bufptr, outBlk, offset, writeLength);
			bufptr += writeLength;
			SysLib.rawwrite(curBlk, outBlk);
//			System.err.println(bufptr + " || " + buffer.length);
		}while(bufptr != buffer.length);
		return bufptr;
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
	
	public synchronized int delete(String filename){			//Delete the filename specified. Waits for transactions to finish first
		FileTableEntry f = open(filename, "w");
		f.inode.toDisk(f.iNumber);
		directory.ifree(f.iNumber);
		close(f);
		return 0;
		//New transactions will not be allowed while waiting to delete
	}
	
	public int fsize(int fd, TCB curr){							//Returns the size (in bytes) of the fd given
		return curr.ftEnt[fd].inode.length;
	}
	
	void sync(){
		FileTableEntry f = open("/", "w");
		byte[] b = directory.directory2bytes();
		write(f,b);
		close(f);
		superblock.sync();
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
	
	private int addBlock(Inode inode) {
		int newBlk = superblock.getFreeBlock();
		int retVal = inode.addBlock(newBlk);
		if (retVal == -2) {
			int newIndirectBlk = superblock.getFreeBlock();
			inode.registerIndirectBlock(newIndirectBlk);
			retVal = inode.addBlock(newBlk);
		}
		return retVal;
	}	
}
