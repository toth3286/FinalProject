import java.util.Arrays;

  /*	Coded by: Jay Hennen & Chris Rouse
 * 	SuperBlock.java
 * 	CSS 430 ~ Professor Fukuda
 */
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
         flag = -1;													//Set the flag to -1 to indicate its initial state
         for ( int i = 0; i < directSize; i++ )						//Set all pointers default to -1 to indicate null
            direct[i] = -1;
         indirect = -1;												//set the indirect value to -1 for null
      }

      /**
       * Reads an Inode from disk and populates fields
       * @param iNumber - inode number to be read
       */
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
    	  
    	  SysLib.rawwrite(blockNumber, data);
    	  return iNodeSize;
      }
      
      /**
       * 
       * @param seekptr
       * @return
       */
      public int findTargetBlock(int seekptr){						//Used to find a target block
    	  if (seekptr > length)									//If the seeker is greater or equal to length, return false
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
    	  for (int i = 0; i < data.length; i+=2) {					//Load the pointers
    		  ptrs[i/2] = SysLib.bytes2short(data, i);
    	  }
    	  return ptrs;
      }
      
      public int addBlock(int newBlk) {								//Called when we want to add another block
    	  int retVal = addDirectBlock(newBlk);						//Try to add a direct link block
    	  if (retVal == -1)											//If direct link fails
    		  retVal = addIndirectPtr(newBlk);						//Allocate an indirect pointer
    	  return retVal;
      }
      
      private int addDirectBlock(int newBlk) {						//Used to attempt to add a direct link block
    	  for (int i = 0; i < direct.length; i++) {					//Iterate over the length of the direct link blocks for an empty spot
    		  if (direct[i]==-1) {									//If the entry is -1
    			  direct[i] = (short)newBlk;						//Create the block and send back the information on it
    			  return newBlk;
    		  }
    	  }
    	  return -1;												//If no empty blocks, return -1 to indicate the direct links are full
      }
      
      private int addIndirectPtr(int newBlk) {						//Used when direct links are full
    	  if (indirect == -1) {										//If indirect is -1, return -2 to indicate to filesystem that we need to allocate the indirect block
    		  return -2;
    	  }
    	  short[] indirects = getIndirectBlock();					//Set up the array of indirect pointers
    	  for (int i = 0; i < indirects.length; i++) {				//Iterate over the array
    		  if (indirects[i]==-1) {								//If we find an empty spot
    			  indirects[i] = (short)newBlk;						//Allocate it
    			  byte[] b = new byte[Disk.blockSize];				//Create a new array
    			  for (int j = 0, k = 0; j < b.length; k++, j+=2) {	//Iterate over j and k to initialize the indirect array so it can be written to disk
    				  SysLib.short2bytes(indirects[k], b, j);		//Convert above data to bytes in preparation for write
    			  }
    			  SysLib.rawwrite(indirect, b);						//Write data to disk
    			  return newBlk;									//Return the new block
    		  }
    	  }
    	  return -1;
      }
      
      public void registerIndirectBlock(int newBlk) {				//Used to register the indirect block
    	  indirect = (short)newBlk;									//Set indirect to the casted newBlock
    	  short[] newIndirect = getIndirectBlock();					//Call to get an indirect block
    	  for (int i = 0; i < newIndirect.length; i++) {			//Iterate through the length of the short array initializing all entries to -1
    		  newIndirect[i] = -1;
    	  }
    	  byte[] b = new byte[Disk.blockSize];						//Create a buffer array
		  for (int j = 0, k = 0; k < newIndirect.length; k++, j+=2) {//Prepare the data for writing by iterating over j and k, setting up the index and value
			  SysLib.short2bytes(newIndirect[k], b, j);				//Convert to bytes for preprocessing of writing to disk
		  }
    	  SysLib.rawwrite(indirect, b);								//Write to the disk
      }
      public String toString() {									//Used for debugging purposes
    	  String outStr = "l: " + length + " cL " + count + " f: " + flag + "\n";
    	  outStr += "direct: " + Arrays.toString(direct) + "\n";
    	  outStr += "indirect: " + Arrays.toString(Arrays.copyOf((indirect != -1) ? getIndirectBlock() : new short[20], 10));
    	  return outStr;
      }
   }