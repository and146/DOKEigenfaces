/**
 * 
 */
package and146.projects.eigenfaces.nativelibs;

/**
 * @author neonards
 *
 */
public enum OperatingSystemEnum {
	LINUX("linux", "lib", "so", "/"),
	WINDOWS("windows", "", "dll", "\\");
	
	private String nativeKey;
	private String libPrefix;
	private String libExtension;
	private String filePathDelimiter;
	
	private OperatingSystemEnum(
			String nativeKey, 
			String libPrefix, 
			String libExtension,
			String filePathDelimiter) {
		this.nativeKey = nativeKey;
		this.libPrefix = libPrefix;
		this.libExtension = libExtension;
		this.filePathDelimiter = filePathDelimiter;
	}
	
	public String getNativeKey() {
		return nativeKey;
	}
	
	public String getLibPrefix() {
		return libPrefix;
	}
	
	public String getLibExtension() {
		return libExtension;
	}
	
	public String getFilePathDelimiter() {
		return filePathDelimiter;
	}
}
