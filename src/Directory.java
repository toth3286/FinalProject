import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Directory {
      private static int maxChars = 30; // max characters of each file name

      // Directory entries
      private byte nameLen[];        // each element represents if index(inode) is associated with a file
      private String[] names;    // each element stores a different file name.
      private Map<String, Integer> namesMap = new HashMap<String,Integer>();
      
      public Directory( int maxInumber ) { // directory constructor
         nameLen = new byte[maxInumber];     // maxInumber = max files
         names = new String[maxInumber];
         names[0] = "/";
         nameLen[0] = 1;
         namesMap.put(names[0], 0);
      }

      public void bytes2directory( byte data[] ) {
         // assumes data[] received directory information from disk
         // initializes the Directory instance with this data[]
    	  int offset = 0;										// Pointer into byte[]
    	  // Reads the first maxInumber bytes and uses them as flags for whether an
    	  // inode is associated with a name (byte value should be 0 or 1)
    	  int sum=0;
    	  for(int i = 0; i < nameLen.length;i++, offset ++){
    		  nameLen[i] = data[i];
    		  sum += nameLen[i];
    	  }
    	  String allNames = new String(data, offset, sum);
    	  int ptr = 0;
    	  for(int i = 0; i < nameLen.length; i++){
    		  if (nameLen[i] != 0) {							// If inode is associated with a file
    			  names[i] = new String(allNames.substring(ptr, ptr+nameLen[i]));
    			  ptr += nameLen[i];
    		  }
    	  }
    	  // make map of names to inode
      }

      // Refactor
      public byte[] directory2bytes( ) {
         // converts and return Directory information into a plain byte array
         // this byte array will be written back to disk
         // note: only meaningfull directory information should be converted
         // into bytes.
    	  int sum = 0;
    	  for (int i = 0; i < nameLen.length; i++) {
    		  sum += nameLen[i];
    	  }
    	  byte[] buffer = new byte[nameLen.length + sum];
    	  
    	  for (int i = 0; i < nameLen.length; i++) {
    		  buffer[i] = nameLen[i];
    	  }
    	  String allNames = "";
    	  for (String s: names) {
    		  allNames.concat(s);
    	  }
    	  byte[] nameBytes = allNames.getBytes();
    	  
    	  for (int i = 0; i < nameBytes.length; i++) {
    		  buffer[i+nameLen.length] = nameBytes[i];
    	  }
    	  
    	  int fd = SysLib.open("/", "w");
    	  SysLib.write(fd, buffer);
    	  return buffer;
      }

      public short ialloc( String filename ) {
    	  assert(filename.length() <= maxChars);
    	  // filename is the one of a file to be created.
    	  // allocates a new inode number for this filename
    	  short i;
    	  for (i = 0; i < nameLen.length; i++) {
    		  if (nameLen[i] == 0) {
    			  break; 
    		  }
    	  }
    	  nameLen[i] = (byte)filename.length();
    	  names[i] = filename;
    	  namesMap.put(filename, (int)i);
    	  return i;
      }

      public boolean ifree( short iNumber ) {
         // deallocates this inumber (inode number)
         // the corresponding file will be deleted.
    	 assert(nameLen[iNumber] != 0); 		//file exists
    	 nameLen[iNumber] = 0;
    	 names[iNumber] = null;
    	 namesMap.remove(names[iNumber]);
    	 return true;
      }

      public short namei( String filename ) {
    	  // returns the inumber corresponding to this filename
    	  return (short)namesMap.get(filename).intValue();
      }
   }