/*	Jay Hennen & Chris Rouse
 * 	SuperBlock.java
 * 	CSS 430 ~ Professor Fukuda
 */


class SuperBlock {
	   private final int defaultInodeBlocks = 64;
      public int totalBlocks; 													// the number of disk blocks
      public int totalInodes; 													// the number of inodes
      public int freeList;    													// the block number of the free list's head
      
      public SuperBlock(int diskSize){										    //Constructor for superblock, takes in disk Size
    	  byte[] superBlock = new byte[Disk.blockSize];							//Buffer
    	  SysLib.rawread(0, superBlock);										//Read data from disk location
    	  totalBlocks = SysLib.bytes2int(superBlock, 0);						//Read total Blocks from disk
    	  totalInodes = SysLib.bytes2int(superBlock, 4);						//Read total Inodes from disk
    	  freeList = SysLib.bytes2int(superBlock, 8);							//Read freeList from disk
    	  
    	  if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)		//Make sure totalBlocks and disksize match and there is more than 2 in the freelist
    		  return;
    	  else{
    		  totalBlocks = diskSize;											//If not equal, must set these equal
    		  SysLib.cerr("Formatting\n");
    		  format();															//and then call format to happen
    	  }
      }
      
      void sync(){
    	  byte[] superBlock = new byte[Disk.blockSize];							//Create buffer
    	  SysLib.rawread(0, superBlock);										//Read from disk into the buffer
    	  SysLib.int2bytes(totalBlocks, superBlock, 0);							//Load total blocks into the buffer as bytes
    	  SysLib.int2bytes(totalInodes, superBlock, 4);							//Load total Inodes into buffer as bytes
    	  SysLib.int2bytes(freeList, superBlock, 8);							//Load freelist into buffer as bytes
    	  SysLib.rawwrite(0, superBlock);										//Write buffer to disk
      }
      
      void format(){
    	  format(defaultInodeBlocks);											//Call format with defaultInodeBlocks
      }
      
      void format(int numInodes){												//Used to format the disk and reestablish superblock and freelist
    	  byte[] buffer = new byte[Disk.blockSize];								//Create the buffer
    	  SysLib.rawread(0, buffer);											//Read the disk into the buffer
    	  totalInodes = numInodes;												
    	  totalBlocks = SysLib.bytes2int(buffer, 0) ;							//set totalBlocks to the return from buffer's int return
    	  freeList = 1;															//Start freelist at 1
    	  
    	  																		// Link blocks into freelist. Final block links to -1 as 'null'
    	  for (short i = 1; i < totalBlocks; i++) {								//Iterate over the total blocks
    		  if (i == totalBlocks - 1)											//If we are at the end, set the next to -1
    			  SysLib.short2bytes((short)-1, buffer, 0);
    		  else																//If not at the end, point to the next block
    			  SysLib.short2bytes((short)(i + 1), buffer, 0);
    		  SysLib.rawwrite(i, buffer);										//Write the change
    	  }
    	  int inodeSize = 0;
    	  																		// Recreates Inodes. Should alter freeList accordingly
    	  for(short i = 0; i < numInodes; i++) {								//Iterate over the number of nodes 
    		  inodeSize = new Inode().toDisk(i);								//Create a new inode and write it to disk
    	  }
    	  freeList += (inodeSize * numInodes)/Disk.blockSize + ((inodeSize * numInodes)%Disk.blockSize > 0 ? 1 : 0);
    	  sync();																//Set freelist to the next free node after the Inodes created
      }
      
      public int getFreeBlock(){												//get a freeblock from the head and return it
    	  int fb = freeList;													//Location of head of freeblocks
    	  byte[] buffer = new byte[Disk.blockSize];								//Buffer creation
    	  SysLib.rawread(fb, buffer);											//Read the information
    	  freeList = SysLib.bytes2short(buffer, 0);								//Set freelist to the short we read
    	  return fb;
      }
      
      public boolean returnBlock(int oldBlockNumber){							//used to return a block to the freepool
    	  byte[] buffer = new byte[Disk.blockSize];								//Create e buffer
    	  SysLib.short2bytes((short)freeList,buffer, 0);						//Read the short into the buffer of the first freenode
    	  SysLib.rawwrite(oldBlockNumber, buffer);								//Write freelist's head into oldBlock's next
    	  freeList = oldBlockNumber;											//Reset the head to the oldBlock
    	  return true;
      }
   }