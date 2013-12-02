
public class FileSystem {
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	public FileSystem(int diskBlocks){
		superblock = new SuperBlock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);
		
		FileTableEntry dirEnt = open("/","r");
		int dirSize = fsize(dirEnt);
		if(dirSize > 0){
			byte[] dirData = new byte[dirSize];
			read(dirEnt, dirData);
			directory.bytes2directory(dirData);
		}
		close(dirEnt);
	}
	
	public int format(int files){
		
	}
	
	public FileTableEntry open(String fileName, String mode){
		FileTableEntry ftEnt = filetable.falloc(fileName, mode);
		if(mode.equalsIgnoreCase("w")){
			if(deallocAllBlocks(ftEnt) == false)
				return null;
		}
		return ftEnt;
	}
	
	public int read(int fd, byte buffer[]){
		
	}
	
	public int write(int fd, byte buffer[]){
		
	}
	
	int seek(int fd, int offset, int whence){
		
	}
	
	int close(int fd){
		
	}
	
	int delete(String fileName){
		
	}
	
	int fsize(int fd){
		
	}
	
	void sync(){

	}
}
