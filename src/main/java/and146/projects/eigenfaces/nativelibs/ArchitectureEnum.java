/**
 * 
 */
package and146.projects.eigenfaces.nativelibs;

/**
 * @author neonards
 *
 */
public enum ArchitectureEnum {
	X86("x86"),
	X86_64("x86-64");
	
	private String nativeKey;
	
	private ArchitectureEnum(String nativeKey) {
		this.nativeKey = nativeKey;
	}
	
	public String getNativeKey() {
		return nativeKey;
	}
}
