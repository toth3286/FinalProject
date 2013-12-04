  public class Inode {
      private final static int iNodeSize = 32;       // fix to 32 bytes
      private final static int directSize = 11;      // # direct pointers

      public int length;                             // file size in bytes
      public short count;                            // # file-table entries pointing to this
      public short flag;                             // 0 = unused, 1 = used, ...
      public short direct[] = new short[directSize]; // direct pointers
      public short indirect;                         // a indirect pointer

      public Inode( ) {                                     // a default constructor
         length = 0;
         count = 0;
         flag = 1;
         for ( int i = 0; i < directSize; i++ )
            direct[i] = -1;
         indirect = -1;
      }

      public Inode( short iNumber ) {                       // retrieving inode from disk
         // design it by yourself.
    	  int blockNumber = 1 + iNumber / 16;
    	  byte[] data = new byte[Disk.blockSize];
    	  SysLib.rawread(blockNumber, data);
    	  int offset = (iNumber % 16) * iNodeSize;
    	  
    	  length = SysLib.bytes2int(data, offset);
    	  offset += 4;
    	  count = SysLib.bytes2short(data, offset);
    	  offset += 2;
    	  flag = SysLib.bytes2short(data, offset);
    	  offset += 2;
    	  
    	  for(int i = 0; i < directSize; i++){
    		  direct[i] = SysLib.bytes2short(data, offset);
    		  offset += 2;
    	  }
    	  indirect = SysLib.bytes2short(data, offset);
      }

      int toDisk( short iNumber ) {                  // save to disk as the i-th inode
    	  int blockNumber = 1 + iNumber / 16;
    	  byte[] data = new byte[Disk.blockSize];
    	  SysLib.rawread(blockNumber, data);
    	  int offset = (iNumber % 16) * iNodeSize;
    	  SysLib.int2bytes(length, data, offset);
    	  offset += 4;
    	  SysLib.short2bytes(count, data, offset);
    	  offset += 2;
    	  SysLib.short2bytes(flag, data, offset);
    	  offset += 2;
    	  
    	  for (int i = 0; i < directSize; i++) {
    		  SysLib.short2bytes(direct[i], data, offset);
    		  offset += 2;
    	  }
    	  SysLib.short2bytes(indirect, data, offset);
    	  
    	  return iNodeSize;
      }
      
      public int findTargetBlock(int seekptr){
    	  if (seekptr >= length)
    		  return -1;
    	  int ptr = seekptr/Disk.blockSize;
    	  if (ptr < 11)
    		  return direct[ptr];
    	  else {
    		  ptr -= 11;
    		  short[] ptrs = getIndirectBlock();
    		  return ptrs[ptr];
    	  }
      }
      
      private short[] getIndirectBlock() {
    	  byte[] data = new byte[Disk.blockSize];
    	  SysLib.rawread(indirect, data);
    	  short[] ptrs = new short[Disk.blockSize/2];
    	  for (int i = 0; i < ptrs.length; i+=2) {
    		  ptrs[i] = SysLib.bytes2short(data, i);
    	  }
    	  return ptrs;
      }
      
      
   }