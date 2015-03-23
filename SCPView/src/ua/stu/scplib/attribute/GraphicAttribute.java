package ua.stu.scplib.attribute;

import java.io.IOException; 

import ua.stu.scplib.structure.SCPECG;
import ua.stu.scplib.structure.Section3;


/**
 * <p>A class that encapsulates the features and values from an SCP ECG source,
 * usually for the purpose of displaying it.</p>
 *
 * @author	dclunie
 */
public class GraphicAttribute extends GraphicAttributeBase {

	/***/
	//private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/displaywave/SCPSourceECG.java,v 1.7 2004/01/25 03:47:19 dclunie Exp $";

	/**
	 */
	protected static String buildInstanceTitle(SCPECG scpecg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(scpecg.getNamedField("LastName"));		// NB. must not contain any embedded or trailing nulls
		buffer.append("^");
		buffer.append(scpecg.getNamedField("FirstName"));
		buffer.append(" [");
		buffer.append(scpecg.getNamedField("PatientIdentificationNumber"));
		buffer.append(" ] - ");
		buffer.append(scpecg.getNamedField("DateOfAcquisition"));
		buffer.append(" ");
		buffer.append(scpecg.getNamedField("TimeOfAcquisition"));
		buffer.append(" [");
		buffer.append(scpecg.getNamedField("ECGSequenceNumber"));
		buffer.append("]");
		return buffer.toString();
	}
	
	/**
	 * <p>Find the named lead in an array of SCP lead codes.</p>
	 *
	 * @param	leadNumbers	an array of the SCP codes used to designate leads
	 * @param	leadName	the string name of the lead wanted
	 * @return			the index in leadNumbers of the requested lead if present, else -1
	 */
	static private int findLead(int[] leadNumbers,String leadName) {
		int scpLeadNumber = Section3.getLeadNumber(leadName);
		for (int i=0; i<leadNumbers.length; ++i) {
			if (leadNumbers[i] == scpLeadNumber) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * <p>Derive additional unipolar leads if possible and necessary.</p>
	 *
	 * <p>Will replace values of samples, numberOfChannels, amplitudeScalingFactorInMilliVolts and channelNames
	 * with longer arrays including the new channels.</p>
	 *
	 * @param	leadNumbers	an array of the SCP codes used to designate leads in the present SCPECG instance
	 */
	private void deriveAdditionalUnipolarLeads(int[] leadNumbers) {
		
		// could hard-code SCPECG numbers, or do string comparison against
		// lead names, but allow for the possibility that the lead names
		// in this.getLeadNames() may not be reliable, and depend on
		// leadNumbers, which is directly from the SCPECG dataset
		
		int indexOfLeadI   = findLead(leadNumbers,"I");
		int indexOfLeadII  = findLead(leadNumbers,"II");
		int indexOfLeadIII = findLead(leadNumbers,"III");
		int indexOfLeadAVR = findLead(leadNumbers,"aVR");
		int indexOfLeadAVL = findLead(leadNumbers,"aVL");
		int indexOfLeadAVF = findLead(leadNumbers,"aVF");
		
		if (indexOfLeadI >= 0 && indexOfLeadII >= 0
		 && indexOfLeadIII < 0 && indexOfLeadAVR < 0
		 && indexOfLeadAVL < 0 && indexOfLeadAVF < 0) {
			// derive everything from leads I and II
			
			// sanity check first ...
			int nSamples = samples[indexOfLeadI].length;
			float scalingFactor = amplitudeScalingFactorInMilliVolts[indexOfLeadI];
			if (nSamples == samples[indexOfLeadII].length && scalingFactor == amplitudeScalingFactorInMilliVolts[indexOfLeadII]) {
				int nextIndex = numberOfChannels;
				numberOfChannels+=4;
				                           samples = ArrayCopyUtilities.expandArray(samples,4);
				amplitudeScalingFactorInMilliVolts = ArrayCopyUtilities.expandArray(amplitudeScalingFactorInMilliVolts,4);
				                      channelNames = ArrayCopyUtilities.expandArray(channelNames,4);
				
				for (int i=nextIndex; i<numberOfChannels; ++i) {
					amplitudeScalingFactorInMilliVolts[i] = scalingFactor;
					samples[i] = new short[nSamples];
				}
				
				indexOfLeadIII = nextIndex++;
				indexOfLeadAVR = nextIndex++;
				indexOfLeadAVL = nextIndex++;
				indexOfLeadAVF = nextIndex;
				
				channelNames[indexOfLeadIII]="III";
				channelNames[indexOfLeadAVR]="aVR";
				channelNames[indexOfLeadAVL]="aVL";
				channelNames[indexOfLeadAVF]="aVF";
				
				for (int sample=0; sample<nSamples; ++sample) {
					int leadI  = samples [indexOfLeadI][sample];
					int leadII = samples[indexOfLeadII][sample];
					int leadIII = leadII - leadI;
					samples[indexOfLeadIII][sample] = (short)(leadIII);
					samples[indexOfLeadAVR][sample] = (short)(-(leadI  + leadII )/2);
					samples[indexOfLeadAVL][sample] = (short)( (leadI  - leadIII)/2);
					samples[indexOfLeadAVF][sample] = (short)( (leadII + leadIII)/2);
				}
			}
			
		}
		
	}
	
	/**
	 * <p>Construct an ECG source from SCP-ECG data an input stream (such as from a file or the network).</p>
	 *
	 * @param	i					the input stream
	 * @param	deriveAdditionalLeads			if true, compute extra unipolar leads when necessary (i.e. make 12 from 8)
	 * @exception	IOException
	 */
	public GraphicAttribute(BinaryInputStream i,boolean deriveAdditionalLeads) throws IOException {
		super();
		SCPECG scpecg = new SCPECG(i,false/*verbose*/);
		if(scpecg.getSection2()==null)
			flNonsection2=true;
		doCommonConstructorStuff(scpecg,deriveAdditionalLeads);
	}
	
	/**
	 * <p>Construct an ECG source from an SCP-ECG instance.</p>
	 *
	 * @param	scpecg					the input stream
	 * @param	deriveAdditionalLeads			if true, compute extra unipolar leads when necessary (i.e. make 12 from 8)
	 * @exception	IOException
	 */
	public GraphicAttribute(SCPECG scpecg,boolean deriveAdditionalLeads) throws IOException {
		super();
		doCommonConstructorStuff(scpecg,deriveAdditionalLeads);
	}
	
	private void doCommonConstructorStuff(SCPECG scpecg,boolean deriveAdditionalLeads) {
		samples = scpecg.getDecompressedRhythmData();
		numberOfChannels = scpecg.getNumberOfLeads();

		nSamplesPerChannel = (int)(scpecg.getNumbersOfSamples()[0]);	// assume all the same, and fits in int :(

		samplingIntervalInMilliSeconds=scpecg.getDecompressedRhythmDataSampleTimeInterval()/1000;	// SCP ECG values is in microseconds
		amplitudeScalingFactorInMilliVolts = new float[numberOfChannels];
		for (int channel=0; channel<numberOfChannels; ++channel) {
			amplitudeScalingFactorInMilliVolts[channel] = (float)0.001;			// SCP ECG decompressed samples are in microvolts
		}
		
		channelNames = scpecg.getLeadNames();
		
		if (deriveAdditionalLeads) {
			deriveAdditionalUnipolarLeads(scpecg.getLeadNumbers());
		}
		
		buildPreferredDisplaySequence(channelNames);
		title=buildInstanceTitle(scpecg);
	}
}
