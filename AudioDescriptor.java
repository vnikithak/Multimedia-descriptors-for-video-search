import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import java.io.*;

public class AudioDescriptor {

	public AudioDescriptor(){

	}

	public String processAudio(String fileName, int frameLength) {
		try {
			AudioFormat audioFormat;
			AudioInputStream audioInputStream = null;
			Info info;
			SourceDataLine dataLine;

			int readBytes = 0;
			byte[] audioBuffer;
			FileInputStream inputStream;

			String audioDescriptor="";

			//read .wav file
			File file = new File(fileName );
			int audiolen = (int)file.length();
			int oneFrameSize = audiolen/frameLength; 
			audioBuffer = new byte[audiolen];
					
			inputStream = new FileInputStream(file);
			InputStream bufferedIn = new BufferedInputStream(inputStream);
			audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
			
			// Obtain the information about the AudioInputStream
			audioFormat = audioInputStream.getFormat();
			info = new Info(SourceDataLine.class, audioFormat);
			
			
			readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
			
			int temp[] = new int[oneFrameSize];
			int index = 0;
			int count = 0;
			int tempMax = 0;
			
			for (int audioByte = 0; audioByte < audiolen;)
			{
					// Do the byte to sample conversion.
					int low = (int) audioBuffer[audioByte];
					audioByte++;
					if (audioByte < audioBuffer.length) {
						int high = (int) audioBuffer[audioByte];
						audioByte++;
						int sample = (high << 8) + (low & 0x00ff);
						temp[count] = sample;
					}
				count++;
				if ((audioByte) % oneFrameSize == 0 || (audioByte + 1) % oneFrameSize == 0) {
					count = 0;
					int maxVal = findMax(temp);
					if (maxVal > tempMax)
						tempMax = maxVal;
					audioDescriptor+=("" + maxVal/1024 + " ");
				}
				
				index++;
			}
			return audioDescriptor;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}	
	}
	
	public int findMax(int[] nums) {
        int maxValue = nums[0];
        for (int i = 0; i < nums.length; i++) {
                if (nums[i]>maxValue) 
                        maxValue = nums[i];
        }
        return maxValue;
	}
}
