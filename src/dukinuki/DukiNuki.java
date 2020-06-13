package dukinuki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;


public class DukiNuki {
    //Make sure to add an option to do full audio renders to prevent mp3 manipulator freaking out when reading unsupported mp3s
    static long everyByte = 0;
    static int selector = 0;
    static int cap = 0;
    
    int timesLooped = 0;

    //A bunch of arrrays that store all the choices set by the user
    private double [] startTimes = new double[]{0.0};
    private double [] fadeIn = new double [] {0.0, 0.0, 0.0, 0.0, 0.0};
    private double [] endTimes = new double[]{0.0};
    private double [] fadeOut = new double [] {0.0, 0.0, 0.0, 0.0, 0.0};
    private boolean [] isFaded = new boolean [] {false, false, false, false,false};
    private double [] startBytes;
    private double [] endBytes;
    private int [] power;
    //0 is for random bytes, 128 is for byte flipping, all other numbers is for byte value changing
    private int [] effectChoice = new int [] {1, 128};

    //Effect choice and power if entire file is to be corrupted
    private int soloEffectChoice;
    private int soloPower;
    
    private boolean isSolo;

    public static long getEveryByte() {
        return everyByte;
    }

    public static void setEveryByte(long everyByte) {
        DukiNuki.everyByte = everyByte;
    }

    public double[] getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(double[] startTimes) {
        this.startTimes = startTimes;
    }

    public double[] getFadeIn() {
        return fadeIn;
    }

    public void setFadeIn(double[] fadeIn) {
        this.fadeIn = fadeIn;
    }

    public double[] getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(double[] endTimes) {
        this.endTimes = endTimes;
    }

    public double[] getFadeOut() {
        return fadeOut;
    }

    public void setFadeOut(double[] fadeOut) {
        this.fadeOut = fadeOut;
    }

    public boolean[] getIsFaded() {
        return isFaded;
    }

    public void setIsFaded(boolean[] isFaded) {
        this.isFaded = isFaded;
    }

    public int[] getEffectChoice() {
        return effectChoice;
    }

    public void setEffectChoice(int[] effectChoice) {
        this.effectChoice = effectChoice;
    }

    public int getSoloEffectChoice() {
        return soloEffectChoice;
    }

    public void setSoloEffectChoice(int soloEffectChoice) {
        this.soloEffectChoice = soloEffectChoice;
    }

    public int getSoloPower() {
        return soloPower;
    }

    public void setSoloPower(int soloPower) {
        this.soloPower = soloPower;
    }

    public boolean isIsSolo() {
        return isSolo;
    }

    public void setIsSolo(boolean isSolo) {
        this.isSolo = isSolo;
    }

    public int[] getPower() {
        return power;
    }

    public void setPower(int[] power) {
        this.power = power;
    }
    
    
    
    public void doEverythingElse(File srcFile, File dstFile) throws BitstreamException, IOException, UnsupportedAudioFileException{
        System.out.println("Running corruptor");
        float duration;
        Random rn = new Random();
        int framesize = (getFrameSize(srcFile)+4);

        
        FileInputStream in = new FileInputStream(srcFile);
        FileOutputStream out = new FileOutputStream(dstFile);
        
        if(isSolo == false){
            getStartAndEndBytes(srcFile);
        }
        
        byte[] buf = new byte[1024];
        byte[] coolio = new byte[1024];

        int len;
        while ((len = in.read(buf)) > 0) {

            int [] manip = new int [buf.length];
            int [] finalManip = new int [buf.length];
            for(int i = 1; i < buf.length; i++){
                manip[i] = buf[i];
            }
            //Here is where you do the funny haha
            //File file,int frameSize, boolean fade, int[]manip, double startByte, double endByte, double fadeIn, double fadeOut, int curveType, int power
            
            //if the user has requested segments then it will run through each one on the original corruptor
            if(isSolo == false){
               manip = corruptor(srcFile,framesize, isFaded[selector], manip, startBytes[selector], endBytes[selector],fadeIn[selector], fadeOut[selector],effectChoice[selector],power[selector]);
            }
            //otherwise it will run the solo corruptor and corrupt the whole file
            else{
                //File file, int[]manip, int effectChoice, int power
               manip = corruptorSolo(srcFile, manip, soloEffectChoice, soloPower); 
            }
            
            //Haha funny ends
            for(int i = 0; i < manip.length; i++){
                coolio[i] = (byte) manip[i];
            }
            //System.out.println(Arrays.toString(coolio));
            out.write(coolio, 0, len);
            //everyByte = everyByte + 1024;
            //System.out.println(timesLooped);
            timesLooped++;
        }

        in.close();
        out.close();
    }
    
    static float getDuration(File audio) throws UnsupportedAudioFileException, IOException{
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audio);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = audio.length();
        int frameSize = format.getFrameSize();
        //System.out.println(frameSize);
        float frameRate = format.getFrameRate();
       // System.out.println(frameRate);
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));
        audioInputStream.close();
        return durationInSeconds;
    }
    
    static float getDuration2(File audio) throws UnsupportedAudioFileException, IOException, BitstreamException{
        Header h= null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(audio);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        Bitstream bitstream = new  Bitstream(file);
        try {
            h = bitstream.readFrame();

        } catch (BitstreamException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        int size = h.calculate_framesize();
            //System.out.println(size);
        float ms_per_frame = h.ms_per_frame();
           // System.out.println(ms_per_frame);
        int maxSize = h.max_number_of_frames(10000);
        float t = h.total_ms(size);
        long tn = 0;
        try {
            tn = file.getChannel().size();
        } catch (IOException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Chanel: " + file.getChannel().size());
        int min = h.min_number_of_frames(500);
        file.close();
        bitstream.close();
        return h.total_ms((int) tn)/1000;
    }
    
    public void getStartAndEndBytes(File srcFile) throws BitstreamException, IOException{
        //gets the start byte, end byte, fade in end, and fade out start for each segment defined by the user
        cap = endTimes.length;
        startBytes = new double [startTimes.length];
        endBytes = new double [endTimes.length];
        for(int i = 0; i < startBytes.length; i++){
            startTimes[i] = startTimes[i] * 1000;
            endTimes[i] = endTimes[i] * 1000;
            //runs only if current selection is to be faded in and out
            if(isFaded[i] == true){
                //convert to miliseconds
                fadeIn[i] = fadeIn[i] * 1000;
                fadeOut[i] = fadeOut[i] * 1000;
                //add fadeIn to start time to get point to end the start curve
                fadeIn[i] = startTimes[i] + fadeIn[i];
                //subtract fadeOut from end time to get time to start the end curve
                fadeOut[i] = endTimes[i] - fadeOut[i];
                //convert fadeIn and fadeOut into byte locations
                fadeIn[i] = (fadeIn[i] / getMSPerFrame(srcFile))*getFrameSize(srcFile);
                fadeOut[i] = (fadeOut[i] / getMSPerFrame(srcFile))*getFrameSize(srcFile);
            }
            startBytes[i] = (startTimes[i] / getMSPerFrame(srcFile))*getFrameSize(srcFile);
            endBytes[i] = (endTimes[i]/ getMSPerFrame(srcFile))*getFrameSize(srcFile);
            System.out.println("Start bytes: " + startBytes[i]);
            System.out.println("End bytes: " + endBytes[i]);
            System.out.println("");
        }
    }
    static int getFrameSize(File audio) throws BitstreamException, IOException{
    Header h= null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(audio);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        Bitstream bitstream = new  Bitstream(file);
        try {
            h = bitstream.readFrame();

        } catch (BitstreamException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        int size = h.calculate_framesize();
        bitstream.close();
        file.close();
        return size;
    }
    
    static double getMSPerFrame(File audio) throws BitstreamException, IOException{
    Header h= null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(audio);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        Bitstream bitstream = new  Bitstream(file);
        try {
            h = bitstream.readFrame();

        } catch (BitstreamException ex) {
            Logger.getLogger(DukiNuki.class.getName()).log(Level.SEVERE, null, ex);
        }
        double ms = h.ms_per_frame();
        bitstream.close();
        file.close();
        return ms;
    }
    static int[]corruptorSolo(File file, int[]manip, int effectChoice, int power){
        for(int i = 0; i < manip.length; i++){            
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 1){
                    //System.out.println("OLD: " + manip[i]);
                    //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 2){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 3){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 4){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 5){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 6){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 7){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 8){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }                    
                    //checks if the current slected time period is to be faded
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 9){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 10){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
        }
        return manip;
    }
    
    static int[] corruptor(File file,int frameSize, boolean fade, int[]manip, double startByte, double endByte, double fadeInEnd, double fadeOutBegin, int effectChoice, int power) throws BitstreamException, IOException{
        for(int i = 0; i < manip.length; i++){
            int currentNumber =0;
            //checks if the current byte selection is to be faded in and out
            if(fade == false){
                if((everyByte) > startByte && (everyByte) < endByte){
                    if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 1){
                    //System.out.println("OLD: " + manip[i]);
                    //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 2){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 3){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 4){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 5){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 6){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 7){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 8){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }                    
                    //checks if the current slected time period is to be faded
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 9){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 10){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
                //System.out.println("NEW: " + manip[i]);
            }
                else if(everyByte > endByte && selector < cap-1){
                    selector++;
                    //System.out.println("yes" + selector);
                    return manip;
                }
            }
            //runs if fade = true
            else{
                //get the duration of fade in and fade out and chop it up into blank # of pieces to be sequenced
                double startChunks = (fadeInEnd - startByte)/power;
                double endChunks = (endByte-fadeOutBegin)/power;
                //if the current byte being process falls between the start of the selected bytes and the end of the fade in then run the code
                if((everyByte) > startByte && (everyByte) < fadeInEnd){
                    //If the current byte is higher then the equally chopped up piece then it will run
                        //Example: if a power level of 5 and the current byte is 3/5 of the way to the end of the fade in
                        //then it will be past 3 of the start chunks and three of the five if statments will try to run per byte
                        
                    //We will call these wacky nested ifs, every comment on this one applies to every one in this if statment and in the fade out if statment
                    //They are all the same
                    if(everyByte > (startChunks) + startByte){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){  
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        //The int current number is for testing. It shows what power the fade in is at if printed at the bottom of the stack of wacky nested if
                        currentNumber = 1;
                    }
                    if(everyByte > ((startChunks * 2) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 2;
                    }
                    if(everyByte > ((startChunks * 3) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 3;
                    }
                    if(everyByte > ((startChunks * 4) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 4;
                    }
                    if(everyByte > ((startChunks * 5) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 5;
                    }
                    if(everyByte > ((startChunks * 6) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 6;
                    }
                    if(everyByte > ((startChunks * 7) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 7;
                    }
                    if(everyByte > ((startChunks * 8) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 8;
                    }
                    if(everyByte > ((startChunks * 9) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                        currentNumber = 9;
                    }
                    if(everyByte > ((startChunks * 10) + startByte)){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                            //System.out.println("NEW: " + manip[i]);
                        }
                    currentNumber = 10;
                    }
                    //System.out.println(currentNumber);
                    
                }
                else if((everyByte) > fadeInEnd && (everyByte) < fadeOutBegin){
                    if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 1){
                    //System.out.println("OLD: " + manip[i]);
                    //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 2){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 3){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 4){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 5){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 6){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 7){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 8){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }                    
                    //checks if the current slected time period is to be faded
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 9){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
            if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1) && power >= 10){
                //System.out.println("OLD: " + manip[i]);
                //effect choice at zero replaces the selected byte with a randomized byte
                if(effectChoice == 0){
                    manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                }
                //128 flips the byte from postive to negative
                else if(effectChoice == 128){
                    manip[i] = manip[i] - (2 * manip[i]);
                }
                //every other number choice (not 0 or 128) is taken and added to the selected byte
                else{
                    //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                    if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                        manip[i] = manip[i] + effectChoice;
                    }
                }
                //System.out.println("NEW: " + manip[i]);
            }
                    
                    //checks if the current slected time period is to be faded
                }
                if((everyByte) > fadeOutBegin && (everyByte) < endByte){
                    //If the current byte is higher then the equally chopped up piece then it will run
                        //Example: if a power level of 5 and the current byte is 3/5 of the way to the end of the fade out
                        //then it will be past 3 of the end chunks and two of the five if statments will try to run per byte
                    if(everyByte < endChunks + fadeOutBegin && power >= 1){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 10;
                    }
                    if(everyByte < ((endChunks * 2) + fadeOutBegin) && power >= 2){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 9;
                    }
                    if(everyByte < ((endChunks * 3) + fadeOutBegin) && power >= 3){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 8;
                    }
                    if(everyByte < ((endChunks*4) + fadeOutBegin) && power >= 4){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 7;
                    }
                    if(everyByte < ((endChunks*5) + fadeOutBegin ) && power >= 5){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 6;
                    }
                    if(everyByte < ((endChunks *6) + fadeOutBegin) && power >= 6){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 5;
                    }
                    if(everyByte < ((endChunks * 7) + fadeOutBegin) && power >= 7){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 4;
                    }
                    if(everyByte < ((endChunks * 8) + fadeOutBegin) && power >= 8){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 3;
                    }
                    if(everyByte < ((endChunks *9) + fadeOutBegin) && power >= 9){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 2;
                    }
                    if(everyByte < ((endChunks * 10) + fadeOutBegin) && power >= 10){
                        if(manip[i] == ThreadLocalRandom.current().nextInt(-128, 127 + 1)){
                            //System.out.println("OLD: " + manip[i]);
                            //effect choice at zero replaces the selected byte with a randomized byte
                            if(effectChoice == 0){
                                manip[i]= ThreadLocalRandom.current().nextInt(-128, 127 + 1);
                            }
                            //128 flips the byte from postive to negative
                            else if(effectChoice == 128){
                                manip[i] = manip[i] - (2 * manip[i]);
                            }
                            //every other number choice (not 0 or 128) is taken and added to the selected byte
                            else{
                                //if statment put in place to prevent overflow (max byte size ranges from -128 to 127)
                                if(manip[i] <= (127 - effectChoice) && manip[i] >= (-128 - effectChoice)){
                                    manip[i] = manip[i] + effectChoice;
                                }
                            }
                        }
                        currentNumber = 1;
                    }
                    
                }
                //When all the code that needed to be edited is edited then increase the selector for all the arrays by one and return the completed byte array
                else if(everyByte > endByte && selector < cap-1){
                    selector++;
                    //System.out.println("yes NOW WITH FADE!!! " + selector);
                    
                    return manip;
                }
            }
                everyByte++;
        }
        //}
        
         return manip;
    }
}

    


            /*

            */
                /*for(int i = 0; i < buf.length; i++){
                    buffer.position(i);
                    if(buffer.getInt() == 0){
                        buffer.putInt(1);
                        System.out.println(buffer.getInt());
                    }
                    
                }*/
