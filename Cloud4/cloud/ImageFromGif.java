//package cloud;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ImageFromGif {

    public static void main(String[] args) throws IOException {

        boolean verbose = false;
        for (String arg:args) {
            if (arg.equals("-v")) verbose=true;
        }
        
        //Get file from argument
        File file = new File(args[0]);

        int frameCount = 0;

        //try to get frames from file to arraylist
        try {
            frameCount = getFrames(file, verbose);
        } catch (Exception e) {
            //print error and stop program on error
            e.printStackTrace();
            return;
        }

        //Initiate output image as first image in array
        File originImage = new File("0cloudTemp.gif");
        BufferedImage outImage = ImageIO.read(originImage);

        //get width, height and framecount
        int width = outImage.getWidth();
        int height = outImage.getHeight();


        //print width, height and framecount
        System.out.println("Width: " + width);
        System.out.println("Height: " + height);
        System.out.println("Frame Count: " + frameCount);


        //loop through all but first frames
        for (int i = 1; i < frameCount; i++) {

            //get current frame
            File imageFile = new File(i + "cloudTemp.gif");
            BufferedImage image = ImageIO.read(imageFile);

            //loop through pixels on x axis
            for (int x = 0; x < width; x++) {

                //loop through pixels on y axis
                for (int y = 0; y < height; y++) {

                    int outColor = outImage.getRGB(x, y);
                    int outBrightness = colorToCombinedRGB(outImage.getRGB(x, y));

                    //System.out.println("frame: "+i+" x: "+x+" y: "+y+" imagewidth: "+image.getWidth()+" originwidth: "+width);
                    int imageColor = image.getRGB(x, y);
                    int imageBrightness = colorToCombinedRGB(image.getRGB(x, y));

                    //System.out.println("frame: "+i+" x: "+x+" y: "+y+" imagebri: "+imageBrightness+" outbri: "+outBrightness);
                    if (imageBrightness < outBrightness) {
                        outImage.setRGB(x, y, imageColor);
                    }
                }
            }

            imageFile.delete();

            if(verbose) System.out.println("Processing Frame: "+(i+1)+"/"+frameCount);
        }

        saveImage(outImage, args[1]);

        originImage.delete();
    }


    public static void saveImage(BufferedImage image, String location) {
        try {
            // retrieve image
            File outputfile = new File(location);
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Takes a strange color value and returns a combined value of 8bit RGB values
     *
     * @param colorIn
     * @return
     */
    public static int colorToCombinedRGB(int colorIn) {
        int red = (colorIn & 0x00ff0000) >> 16;
        int green = (colorIn & 0x0000ff00) >> 8;
        int blue = colorIn & 0x000000ff;

        int outBrightness = red + blue + green;

        return outBrightness;
    }

    /**
     * got this from stackoverflow https://stackoverflow.com/a/10627458
     * don't know how it works but it does
     *
     * @param gif
     * @return
     * @throws IOException
     */
    public static int getFrames(File gif, boolean verbose) throws IOException {

        int frameCount = 0;
        try {
            String[] imageatt = new String[]{
                    "imageLeftPosition",
                    "imageTopPosition",
                    "imageWidth",
                    "imageHeight"
            };

            ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
            ImageInputStream ciis = ImageIO.createImageInputStream(gif);
            reader.setInput(ciis, false);

            int noi = reader.getNumImages(true);

            frameCount = noi;

            //BufferedImage master = null;
            BufferedImage master = null;

            for (int i = 0; i < noi; i++) {
                BufferedImage image = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);

                Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
                NodeList children = tree.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node nodeItem = children.item(j);

                    if(nodeItem.getNodeName().equals("ImageDescriptor")){
                        Map<String, Integer> imageAttr = new HashMap<String, Integer>();

                        for (int k = 0; k < imageatt.length; k++) {
                            NamedNodeMap attr = nodeItem.getAttributes();
                            Node attnode = attr.getNamedItem(imageatt[k]);
                            imageAttr.put(imageatt[k], Integer.valueOf(attnode.getNodeValue()));
                        }
                        if(i==0){
                            master = new BufferedImage(imageAttr.get("imageWidth"), imageAttr.get("imageHeight"), BufferedImage.TYPE_INT_ARGB);
                        }
                        master.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"), imageAttr.get("imageTopPosition"), null);
                    }
                }

                ImageIO.write((RenderedImage) master, "GIF", new File( i + "cloudTemp.gif"));

                if(verbose) System.out.println("Generating Frame: "+(i+1)+"/"+noi);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return frameCount;
    }
}
