package ua.stu.scplib.attribute;

/**
 * <p>A class to provide enumerated constants for the entities of the DICOM Information Model.</p>
 *
 * <p>Used to categorize attributes in the {@link com.pixelmed.dicom.DicomDictionary DicomDictionary} and
 * in the {@link com.pixelmed.database com.pixelmed.database} package.</p>
 *
 * @author	dclunie
 */
public class InformationEntity implements Comparable {

	/***/
	private int value;

	/**
	 * @param	value
	 */
	private InformationEntity(int value) {
		this.value=value;
	}

	/***/
	public static InformationEntity PATIENT       = new InformationEntity(2);
	/***/
	public static InformationEntity STUDY         = new InformationEntity(3);
	/***/
	public static InformationEntity PROCEDURESTEP = new InformationEntity(4);
	/***/
	public static InformationEntity SERIES        = new InformationEntity(5);
	/***/
	public static InformationEntity CONCATENATION = new InformationEntity(6);
	/***/
	public static InformationEntity INSTANCE      = new InformationEntity(7);
	/***/
	public static InformationEntity FRAME         = new InformationEntity(8);

	/***/
	public String toString() {
		if      (this == PATIENT)       return "Patient";
		else if (this == STUDY)         return "Study";
		else if (this == PROCEDURESTEP) return "ProcedureStep";
		else if (this == SERIES)        return "Series";
		else if (this == CONCATENATION) return "Concatenation";
		else if (this == INSTANCE)      return "Instance";
		else if (this == FRAME)         return "Frame";
		else return null;
	}
	
	/**
	 * <p>Get the information entity corresponding to the string name ? </p>
	 *
	 * @param	test	a String name, whose case is ignored
	 * @return		the information entity if any, otherwise null
	 */
	public static InformationEntity fromString(String test) {
		if (test != null) {
			String name = test.toUpperCase();
			if      (name.equals("PATIENT"))       return PATIENT;
			else if (name.equals("STUDY"))         return STUDY;
			else if (name.equals("PROCEDURESTEP")) return PROCEDURESTEP;
			else if (name.equals("SERIES"))        return SERIES;
			else if (name.equals("CONCATENATION")) return CONCATENATION;
			else if (name.equals("INSTANCE"))      return INSTANCE;
			else if (name.equals("FRAME"))         return FRAME;
			else return null;
		}
		else return null;
	}
	
	/***/
	public int getValue() { return value; }
	
	/**
	 * <p>Is this information entity higher in the model than the specified information entity ?</p>
	 *
	 * @param	ie	the information entity with which to compare
	 * @return		a -ve value if this information entity higher in the model than the specified information entity
	 */
	public int compareTo(Object ie) {
		return value - ((InformationEntity)ie).getValue();
	}
}



