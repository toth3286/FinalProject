/*	Jay Hennen & Chris Rouse
 * 	SuperBlock.java
 * 	CSS 430 ~ Professor Fukuda
 */

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Directory {
      private static int maxChars = 30; 						// max characters of each file name
      															// Directory entries
      private byte nameLen[];        							// each element represents if index(inode) is associated with a file
      private String[] names;    								// each element stores a different file name.
      private Map<String, Integer> namesMap = new HashMap<String,Integer>();
      
      public Directory( int maxInumber ) { 						// directory constructor
         nameLen = new byte[maxInumber];     					// maxInumber = max files
         names = new String[maxInumber];
         names[0] = "/";
         nameLen[0] = 1;
         namesMap.put(names[0], 0);
      }

		// converts and plain byte array information into a directory
		// this byte array will be read from the disk
      public void bytes2directory( byte data[] ) {
    	  														// assumes data[] received directory information from disk
    	  														// initializes the Directory instance with this data[]
    	  int offset = 0;										// Pointer into byte[]

    	  int sum=0;
    	  for(int i = 0; i < nameLen.length;i++, offset ++){    // Reads the first maxInumber bytes and uses them as flags for whether an
    		  nameLen[i] = data[i];								// inode is associated with a name (byte value should be 0 or 1)
    		  sum += nameLen[i];
    	  }
    	  String allNames = new String(data, offset, sum);		//String to hold all names
    	  int ptr = 0;											
    	  for(int i = 0; i < nameLen.length; i++){
    		  if (nameLen[i] != 0) {							// If inode is associated with a file
    			  names[i] = new String(allNames.substring(ptr, ptr+nameLen[i]));
    			  ptr += nameLen[i];							//create a new string with the information and increment ptr
    		  }
    	  }
    	  // make map of names to inode
      }

      	// Refactor
		// converts and return Directory information into a plain byte array
		// this byte array will be written back to disk
		// note: only meaningfull directory information should be converted
		// into bytes.
      public byte[] directory2bytes( ) {

    	  int sum = 0;											
    	  for (int i = 0; i < nameLen.length; i++) {			//calculate the sum
    		  sum += nameLen[i];
    	  }
    	  byte[] buffer = new byte[nameLen.length + sum];		//Use the sum in the calculation of the byte array's length
    	  
    	  for (int i = 0; i < nameLen.length; i++) {			//copy the namelen variables into buffer
    		  buffer[i] = nameLen[i];
    	  }
    	  String allNames = "";									//Default string for allNames
    	  for (String s: names) {								//While iterating over names, concat them all into a single string
    		  allNames.concat(s);
    	  }
    	  byte[] nameBytes = allNames.getBytes();				//Convert the string into a byte array
    	  
    	  for (int i = 0; i < nameBytes.length; i++) {			//Load the data into buffer
    		  buffer[i+nameLen.length] = nameBytes[i];
    	  }
    	  
    	  int fd = SysLib.open("/", "w");						//Open the file
    	  SysLib.write(fd, buffer);								//Write the buffer to disk
    	  return buffer;
      }

      public short ialloc( String filename ) {					// filename is the one of a file to be created.
    	  assert(filename.length() <= maxChars);
    	  short i;												// allocates a new inode number for this filename
    	  for (i = 0; i < nameLen.length; i++) {				// When we find an empty spot, we can use it, so break the loop
    		  if (nameLen[i] == 0) {
    			  break; 
    		  }
    	  }
    	  nameLen[i] = (byte)filename.length();					//Set the filename length
    	  names[i] = filename;									//set the filename
    	  namesMap.put(filename, (int)i);						//Place the data into the map
    	  return i;
      }

      public boolean ifree( short iNumber ) {					// deallocates this inumber (inode number), the corresponding file will be deleted.
    	 assert(nameLen[iNumber] != 0); 						//file exists
    	 nameLen[iNumber] = 0;									//set the length to zero
    	 names[iNumber] = null;									//change name to null
    	 namesMap.remove(names[iNumber]);						//remove from the map
    	 return true;
      }

      public short namei( String filename ) {
    	  // returns the inumber corresponding to this filename
    	  Integer retVal = namesMap.get(filename);
    	  if (retVal != null)
    		  return (short)retVal.intValue();
    	  else
    		  return -1;
      }
   }