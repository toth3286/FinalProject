
public class FileSystem {
	private int diskBlocks;
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	public FileSystem(int diskBlocks){
		this.diskBlocks = diskBlocks;
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
		byte[] b = new byte[1000];
		SysLib.int2bytes(diskBlocks, b, 0);
		SysLib.rawwrite(0, b);
		superblock.format(files);
		return 0;
	}
	
	public FileTableEntry open(String fileName, String mode){
		FileTableEntry ftEnt = filetable.falloc(fileName, mode);
		if(mode.equalsIgnoreCase("w")){
			if(deallocAllBlocks(ftEnt) == false)
				return null;
		}
		return ftEnt;
	}
	
	public synchronized int read(int fd, byte buffer[]){
		
	}
	
	public synchronized int write(int fd, byte buffer[]){
		
	}
	
	public synchronized int seek(FileTableEntry fd, int offset, int whence){
		if(whence < 1){
//			fd.seekPtr
		}else if(whence == 1){
			
		}else if(whence == 2){
			
		}else{
			
		}
	}
	
	public synchronized int close(int fd){
		
	}
	
	public synchronized int delete(String fileName){
		
	}
	
	int fsize(int fd){
		
	}
	
	void sync(){

	}
}
