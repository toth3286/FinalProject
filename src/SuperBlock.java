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
    	  byte[] superBlock = new byte[Disk.blockSize];
    	  SysLib.rawread(0, superBlock);
    	  SysLib.int2bytes(totalBlocks, superBlock, 0);
    	  SysLib.int2bytes(totalInodes, superBlock, 4);
    	  SysLib.int2bytes(freeList, superBlock, 8);
    	  SysLib.rawwrite(0, superBlock);
      }
      
      void format(){
    	  format(defaultInodeBlocks);
      }
      
      void format(int numInodes){
    	  byte[] buffer = new byte[Disk.blockSize];
    	  SysLib.rawread(0, buffer);
    	  totalInodes = numInodes;
    	  totalBlocks = SysLib.bytes2int(buffer, 0) ;
    	  freeList = 1;
    	  
    	  // Link blocks into freelist. Final block links to -1 as 'null'
    	  for (short i = 1; i < totalBlocks; i++) {
    		  if (i == totalBlocks - 1)
    			  SysLib.short2bytes((short)-1, buffer, 0);
    		  else
    			  SysLib.short2bytes((short)(i + 1), buffer, 0);
    		  SysLib.rawwrite(i, buffer);
    	  }
    	  int inodeSize = 0;
    	  // Recreates Inodes. Should alter freeList accordingly
    	  for(short i = 0; i < numInodes; i++) {
    		  inodeSize = new Inode().toDisk(i);
    	  }
    	  freeList += (inodeSize * numInodes)/Disk.blockSize + ((inodeSize * numInodes)%Disk.blockSize > 0 ? 1 : 0);
    	  sync();
      }
      
      public int getFreeBlock(){
    	  int fb = freeList;
    	  byte[] buffer = new byte[Disk.blockSize];
    	  SysLib.rawread(fb, buffer);
    	  freeList = SysLib.bytes2short(buffer, 0);
    	  return fb;
      }
      
      public boolean returnBlock(int oldBlockNumber){
    	  byte[] buffer = new byte[Disk.blockSize];
    	  SysLib.short2bytes((short)freeList,buffer, 0);
    	  SysLib.rawwrite(oldBlockNumber, buffer);
    	  freeList = oldBlockNumber;
    	  return true;
      }
   }