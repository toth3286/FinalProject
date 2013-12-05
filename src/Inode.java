  public class Inode {
      private final static int iNodeSize = 32;       				// fix to 32 bytes
      private final static int directSize = 11;      				// # direct pointers

      public int length;                             				// file size in bytes
      public short count;                            				// # file-table entries pointing to this
      public short flag;                             				// 0 = unused, 1 = used, ...
      public short direct[] = new short[directSize]; 				// direct pointers
      public short indirect;                         				// a indirect pointer

      public Inode( ) {                                     		// a default constructor
         length = 0;												//Set all values to their default values
         count = 0;
         flag = 1;													//Set the flag to -1 to indicate its initial state
         for ( int i = 0; i < directSize; i++ )						//Set all pointers default to -1 to indicate null
            direct[i] = -1;
         indirect = -1;												//set the indirect value to -1 for null
      }

      public Inode( short iNumber ) {                       		//retrieving inode from disk

    	  int blockNumber = 1 + iNumber / 16;						//set the block number, divisible by 16. Add 1 to keep out of superblock
    	  byte[] data = new byte[Disk.blockSize];					//Create buffer
    	  SysLib.rawread(blockNumber, data);						//Read data from the buffer
    	  int offset = (iNumber % 16) * iNodeSize;					//The offset is the inumber modded by 16. Will always produce scaled value
    	  
    	  length = SysLib.bytes2int(data, offset);					//Read in the length and move the offset
    	  offset += 4;
    	  count = SysLib.bytes2short(data, offset);					//Read in the count and move the offset
    	  offset += 2;
    	  flag = SysLib.bytes2short(data, offset);					//Read in the flag and move the offset
    	  offset += 2;
    	  
    	  for(int i = 0; i < directSize; i++){						//Read in the short data for direct pointer
    		  direct[i] = SysLib.bytes2short(data, offset);
    		  offset += 2;
    	  }
    	  indirect = SysLib.bytes2short(data, offset);				//Read in the indirect 
      }

      int toDisk( short iNumber ) {                  				// save to disk as the i-th inode
    	  int blockNumber = 1 + iNumber / 16;						//set the block number, divisible by 16. Add 1 to keep out of superblock
    	  byte[] data = new byte[Disk.blockSize];					//Buffer
    	  SysLib.rawread(blockNumber, data);						//Read in the buffer
    	  int offset = (iNumber % 16) * iNodeSize;					//Calculate the offset
    	  SysLib.int2bytes(length, data, offset);					//Prepare the length	
    	  offset += 4;	
    	  SysLib.short2bytes(count, data, offset);					//Prepare the count
    	  offset += 2;
    	  SysLib.short2bytes(flag, data, offset);					//Prepare the flag
    	  offset += 2;
    	  
    	  for (int i = 0; i < directSize; i++) {					//Set all direct pointers
    		  SysLib.short2bytes(direct[i], data, offset);
    		  offset += 2;
    	  }
    	  SysLib.short2bytes(indirect, data, offset);				//Set indirect pointer
    	  
    	  return iNodeSize;
      }
      
      public int findTargetBlock(int seekptr){						//Used to find a target block
    	  if (seekptr >= length)									//If the seeker is greater or equal to length, return false
    		  return -1;
    	  int ptr = seekptr/Disk.blockSize;
    	  if (ptr < 11)												//If the seek pointer is within the 11 direct pointers
    		  return direct[ptr];									//return back the direct pointer
    	  else {
    		  ptr -= 11;											//If the pointer isn't less than 11, it's an indirect
    		  short[] ptrs = getIndirectBlock();					//create a place for the pointers and call getIndirectBlock
    		  return ptrs[ptr];										//return the pointer
    	  }
      }
      
      private short[] getIndirectBlock() {							//Used to get the location within the indirect block
    	  byte[] data = new byte[Disk.blockSize];					//Create the buffer
    	  SysLib.rawread(indirect, data);							//Read the indirect data from the disk
    	  short[] ptrs = new short[Disk.blockSize/2];				//create an array of pointers
    	  for (int i = 0; i < ptrs.length; i+=2) {					//Load the pointers
    		  ptrs[i] = SysLib.bytes2short(data, i);
    	  }
    	  return ptrs;
      }
      
      
   }