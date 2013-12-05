import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Directory {
      private static int maxChars = 30; // max characters of each file name

      // Directory entries
      private byte hasName[];        // each element represents if index(inode) is associated with a file
      private String[] names;    // each element stores a different file name.
      private Map<String, Integer> namesMap = new HashMap<String,Integer>();
      
      public Directory( int maxInumber ) { // directory constructor
         hasName = new byte[maxInumber];     // maxInumber = max files
         names = new String[maxInumber];
         names[0] = "/";
         hasName[0] = 1;
         namesMap.put(names[0], 0);
      }

      public void bytes2directory( byte data[] ) {
         // assumes data[] received directory information from disk
         // initializes the Directory instance with this data[]
    	  int offset = 0;										// Pointer into byte[]
    	  // Reads the first maxInumber bytes and uses them as flags for whether an
    	  // inode is associated with a name (byte value should be 0 or 1)
    	  for(int i = 0; i < hasName.length;i++, offset ++){
    		  hasName[i] = data[i];
    	  }
    	  
    	  for(int i = 0; i < names.length; i++){
    		  if (hasName[i] == 1) {							// If inode is associated with a file
    			  char[] buffer = new char[maxChars];			// char buffer to gather string
    			  int j = 0;									// pointer for char buffer
    			  short cur = SysLib.bytes2short(data, offset);	
    			  while (cur != 0) {							// read chars from byte[] until '/0' char
    				  buffer[j] = (char)cur;					// store char into buffer
    				  offset+=2;
    				  j++;
    				  cur = SysLib.bytes2short(data, offset);	// get next char
    			  }
    			  offset+=2;
    			  names[i] = new String(buffer,0,j);			// make String from buffer and store
    		  }
    	  }
    	  // make map of names to inode
      }

      public byte[] directory2bytes( ) {
         // converts and return Directory information into a plain byte array
         // this byte array will be written back to disk
         // note: only meaningfull directory information should be converted
         // into bytes.
    	  
    	  
      }

      public short ialloc( String filename ) {
    	  assert(filename.length() <= maxChars);
    	  // filename is the one of a file to be created.
    	  // allocates a new inode number for this filename
    	  short i;
    	  for (i = 0; i < hasName.length; i++) {
    		  if (hasName[i] == 0) {
    			  break; 
    		  }
    	  }
    	  hasName[i] = 1;
    	  names[i] = filename;
    	  namesMap.put(filename, (int)i);
    	  return i;
      }

      public boolean ifree( short iNumber ) {
         // deallocates this inumber (inode number)
         // the corresponding file will be deleted.
    	 assert(hasName[iNumber] == 1); 		//file exists
    	 hasName[iNumber] = 0;
    	 names[iNumber] = null;
    	 namesMap.remove(names[iNumber]);
    	 return true;
      }

      public short namei( String filename ) {
    	  // returns the inumber corresponding to this filename
    	  return (short)namesMap.get(filename).intValue();
      }
   }