   class SuperBlock {
	   private final int defaultInodeBlocks = 64;
      public int totalBlocks; // the number of disk blocks
      public int totalInodes; // the number of inodes
      public int freeList;    // the block number of the free list's head
      
      public SuperBlock(int diskSize){
    	  byte[] superBlock = new byte[Disk.blockSize];
    	  SysLib.rawread(0, superBlock);
    	  totalBlocks = SysLib.bytes2int(superBlock, 0);
    	  totalInodes = SysLib.bytes2int(superBlock, 4);
    	  freeList = SysLib.bytes2int(superBlock, 8);
    	  
    	  if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
    		  return;
    	  else{
    		  totalBlocks = diskSize;
    		  SysLib.cerr("Formatting\n");
    		  format();
    	  }
      }
      
      void sync(){
    	  
      }
      
      void format(){
    	  
      }
      
      void format(int numBlocks){
    	  
      }
      
      public int getFreeBlock(){
    	  
      }
      
      public boolean returnBlock(int oldBlockNumber){
    	  
      }
   }