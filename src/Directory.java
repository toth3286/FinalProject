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
    	  int offset = 0;
    	  for(int i = 0; i < hasName.length;i++, offset ++){
    		  hasName[i] = data[i];
    	  }
    	  
    	  for(int i = 0; i < names.length; i++){
    		  if (hasName[i] == 1) {
    			  char[] buffer = new char[maxChars];
    			  int j = 0;
    			  short cur = SysLib.bytes2short(data, offset);
    			  while (cur != 0) {
    				  buffer[j] = (char)cur;
    				  offset+=2;
    				  j++;
    				  cur = SysLib.bytes2short(data, offset);
    			  }
    			  offset+=2;
    			  names[i] = new String(buffer,0,j);
    		  }
    	  }
      }

      public byte[] directory2bytes( ) {
         // converts and return Directory information into a plain byte array
         // this byte array will be written back to disk
         // note: only meaningfull directory information should be converted
         // into bytes.
      }

      public short ialloc( String filename ) {
         // filename is the one of a file to be created.
         // allocates a new inode number for this filename
      }

      public boolean ifree( short iNumber ) {
         // deallocates this inumber (inode number)
         // the corresponding file will be deleted.
      }

      public short namei( String filename ) {
         // returns the inumber corresponding to this filename
      }
   }