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
		
		FileTableEntry dirEnt = open("/","r");					//Open the file related to the root with read flagged
		int dirSize = fsize(dirEnt);							//the directory size is set to the filesize of the directory entry
		if(dirSize > 0){										//If the size is greater than zero
			byte[] dirData = new byte[dirSize];					//Create a buffer
			read(dirEnt, dirData);								//Read the data
			directory.bytes2directory(dirData);					//read in the directory
		}
		close(dirEnt);											//Close the directory entry
	}
	
	public int format(int files){								//Format the FileSystem, and by association, the superblock
		byte[] b = new byte[1000];								//Create a new disk buffer
		SysLib.int2bytes(diskBlocks, b, 0);						//Convery to bytes
		SysLib.rawwrite(0, b);									//Write the data to the disk
		superblock.format(files);								//Call the superblock format function
		return 0;
	}
	
	public FileTableEntry open(String fileName, String mode){	//Used to open a file
		FileTableEntry ftEnt = filetable.falloc(fileName, mode);//Allocate a new file with the name and the mode
		if(mode.equalsIgnoreCase("w")){							//If the mode is set to write
			if(deallocAllBlocks(ftEnt) == false)				//If you can't deallocate all blocks
				return null;									//Return null
		}
		return ftEnt;											//Otherwise retutn the filetableentry
	}
	
	public synchronized int read(int fd, byte buffer[]){
		
	}
	
	public synchronized int write(int fd, byte buffer[]){
		
	}
	
	public synchronized int seek(FileTableEntry fd, int offset, int whence){
		if(whence < 1){											//if whence is less than one, we start from offset from the beginning of the file
//			fd.seekPtr
		}else if(whence == 1){									//if whence is one, we start from the current pointer location plus offset
			
		}else if(whence == 2){									//If set to two, we set to the size of the file plus the offset (can be positive/negative)
			
		}else{													//If greater than two, we set pointer to end of file
			
		}
	}
	
	public synchronized int close(int fd){						//Closes the entry pertaining to fd, commits all file transations, 
																//unregisters from fd table, 
	}
	
	public synchronized int delete(String fileName){			//Delete the filename specified. Waits for transactions to finish first
																//New transactions will not be allowed while waiting to delete
	}
	
	int fsize(int fd){											//Returns the size (in bytes) of the fd given
		
	}
	
	void sync(){

	}
}
