/*
    Copyright (c) 2006-2013 [Joerg Ruedenauer]
  
    This file is part of Heldenverwaltung.

    Heldenverwaltung is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Heldenverwaltung is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Heldenverwaltung; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.util;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sounds {
	
		public static void play(final String filename) {
			Thread t = new Thread(new Runnable( ) {
				public void run() {
					try {
			            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(filename));
			            AudioFormat format;
			            format = audio.getFormat();
			            SourceDataLine auline;
			            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			            auline = (SourceDataLine) AudioSystem.getLine(info);
			            auline.open(format);
			            auline.start();
			            int nBytesRead = 0;
			            byte[] abData = new byte[524288];
			            while (nBytesRead != -1) {
			                nBytesRead = audio.read(abData, 0, abData.length);
			                if (nBytesRead >= 0) {
			                    auline.write(abData, 0, nBytesRead);
			                }
			            }
					} catch (LineUnavailableException e) {
					} catch (IOException e) {
					} catch (UnsupportedAudioFileException e) {
					}
				}
			});
			t.start();
	}
	
	private Sounds() {}
}
