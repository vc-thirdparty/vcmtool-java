package com.github.redsolo.vcm.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;

/**
 * Targa image reader, copied from
 * http://stackoverflow.com/questions/1514035/java-tga-loader
 * http://stackoverflow.com/questions/665406/how-to-make-a-color-transparent-in-a-bufferedimage-and-save-as-png
 */
@SuppressWarnings("all")
public class TargaReader {
	private static int offset;

	private static int btoi(byte b) {
		int a = b;
		return (a < 0 ? 256 + a : a);
	}

	private static int read(byte[] buf) {
		return btoi(buf[offset++]);
	}

	public static Image decode(byte[] buf) throws IOException {
		offset = 0;

		// Reading header bytes
		// buf[2]=image type code 0x02=uncompressed BGR or BGRA
		// buf[12]+[13]=width
		// buf[14]+[15]=height
		// buf[16]=image pixel size 0x20=32bit, 0x18=24bit
		// buf{17]=Image Descriptor Byte=0x28 (00101000)=32bit/origin
		// upperleft/non-interleaved
		for (int i = 0; i < 12; i++)
			read(buf);
		int width = read(buf) + (read(buf) << 8); // 00,04=1024
		int height = read(buf) + (read(buf) << 8); // 40,02=576
		read(buf);
		read(buf);

		int n = width * height;
		int[] pixels = new int[n];
		int idx = 0;

		if (buf[2] == 0x02 && buf[16] == 0x20) { // uncompressed BGRA
			while (n > 0) {
				int b = read(buf);
				int g = read(buf);
				int r = read(buf);
				int a = read(buf);
				int v = (a << 24) | (r << 16) | (g << 8) | b;
				pixels[idx++] = v;
				n -= 1;
			}
		} else if (buf[2] == 0x02 && buf[16] == 0x18) { // uncompressed BGR
			while (n > 0) {
				int b = read(buf);
				int g = read(buf);
				int r = read(buf);
				int a = 255; // opaque pixel
				int v = (a << 24) | (r << 16) | (g << 8) | b;
				pixels[idx++] = v;
				n -= 1;
			}
		} else {
			// RLE compressed
			while (n > 0) {
				int nb = read(buf); // num of pixels
				if ((nb & 0x80) == 0) { // 0x80=dec 128, bits 10000000
					for (int i = 0; i <= nb; i++) {
						int b = read(buf);
						int g = read(buf);
						int r = read(buf);
						pixels[idx++] = 0xff000000 | (r << 16) | (g << 8) | b;
					}
				} else {
					nb &= 0x7f;
					int b = read(buf);
					int g = read(buf);
					int r = read(buf);
					int v = 0xff000000 | (r << 16) | (g << 8) | b;
					for (int i = 0; i <= nb; i++)
						pixels[idx++] = v;
				}
				n -= nb + 1;
			}
		}

		BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		source.setRGB(0, 0, width, height, pixels, 0, width);
		
		int color = source.getRGB(0, 0);
    	return makeColorTransparent(source, new Color(color));
	}

	public static Image makeColorTransparent(BufferedImage im, final Color color) {
		ImageFilter filter = new RGBImageFilter() {

			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
}