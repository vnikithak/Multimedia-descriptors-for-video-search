import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JPanel;

//import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class OnlineUI extends Frame implements ActionListener, ChangeListener {

    private  ArrayList<BufferedImage> dbImages;
    private JLabel imageLabel,resultImageLabel,errorLabel,matchLabel,frameLabel,frameNumLabel;
    private JPanel panel;
    int[] sortedIndices;
    private JSlider visualSlider;
    private TextField queryField;
    private Button playButton,pauseButton,stopButton,resultPlayButton,resultPauseButton,resultStopButton,searchButton;
    private List resultListDisplay;
    private ArrayList<Double> resultList;
    private ArrayList<String> resultListRankedNames;
    private ArrayList<BufferedImage> images;
    private String fileFolder = "/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/query";
    private String dbFileFolder = "/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_rgb";
    private String dbAudioFolder = "/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_wav";
    private int playStatus = 3; //1 for play, 2 for pause, 3 for stop
    private int resultPlayStatus = 3;
    private Thread playingThread,playingDBThread,audioThread,audioDBThread;

    private int totalFrameNum = 480;
    private int currentFrameNum = 0;
    private int currentDBFrameNum = 0;
    static final int WIDTH = 640;
    static final int HEIGHT = 360;
    private PlaySound playSound;
    private PlaySound playDBSound;
    String[] videoFiles;
    String[] audioFiles;
    String[] displayFileNames;
    public int queryCount = 50;

    DrawGraph drawGraph;


    int videoCount = 29;
    double[][] sumDiff= new double[videoCount][480];
    String[][] colorIndex = new String[videoCount][480];
    int[][] audioIndex = new int[videoCount][480];
    int[][] motionIndex = new int[videoCount][480];
    boolean readInIndex = false;


    public OnlineUI(){


        JFrame frame = new JFrame();
        sortedIndices = new int[this.videoCount];
        this.videoFiles = new String[videoCount];
        this.audioFiles = new String[videoCount];
        this.displayFileNames = new String[videoCount];

        Panel controlQueryPanel = new Panel();
        controlQueryPanel.setPreferredSize(new Dimension(1500,325));
        controlQueryPanel.setLayout(new GridLayout(0, 2));

        //get video files
        File videoFolder = new File(dbFileFolder);
        File[] listOfVideoDirs = videoFolder.listFiles((dir, name) -> !name.equals(".DS_Store"));
        Arrays.sort(listOfVideoDirs);
        int c=0;
        for(int i=0; i<listOfVideoDirs.length;i++){
            //videoDirs[i]=ads,sports
            if (listOfVideoDirs[i].isDirectory()) {
                //list of videos are ads_0 etc
                File[] listOfVideos = listOfVideoDirs[i].listFiles((dir, name) -> !name.equals(".DS_Store"));
                Arrays.sort(listOfVideos);
                String dirName=listOfVideoDirs[i].getName();
                for (int j = 0; j < listOfVideos.length; j++) {
                    videoFiles[c]="/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_rgb/"+dirName+"/"+listOfVideos[j].getName();
                    displayFileNames[c] = listOfVideos[j].getName()+".rgb";
                    c++;
                }
            }
        }

        c=0;
        //get audio files
        File audioFolder = new File(dbAudioFolder);
        File[] listOfAudioDirs = audioFolder.listFiles();
        Arrays.sort(listOfAudioDirs);
        for(int i=0; i<listOfAudioDirs.length;i++){
            //audio[i]=ads,sports
            if (listOfAudioDirs[i].isDirectory()) {
                //list of videos are ads_0.wav etc
                File[] listOfAudios = listOfAudioDirs[i].listFiles((dir, name) -> !name.equals(".DS_Store"));
                Arrays.sort(listOfAudios);
                String dirName=listOfAudioDirs[i].getName();
                for (int j = 0; j < listOfAudios.length; j++) {
                    audioFiles[c]="/Users/nikithavemulapalli/Desktop/Courses/CSCI 576-Multimedia/Assignments/Project/Data_wav/"+dirName+"/"+listOfAudios[j].getName();
                    c++;
                }
            }
        }
        

        //Query Panel
        Panel queryPanel = new Panel();
        queryField = new TextField(25);
        queryField.addActionListener(this);
        JLabel queryLabel = new JLabel("Query: ");
        queryPanel.add(queryLabel);
        queryPanel.add(queryField);
        queryPanel.setPreferredSize(new Dimension(750,300));
        searchButton = new Button("Search");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 15));
        searchButton.addActionListener(this);
        queryPanel.add(searchButton);

        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        frameLabel = new JLabel("");
        frameLabel.setForeground(Color.BLUE);

        //add list to query panel
        matchLabel = new JLabel("");
        matchLabel.setText("Matched Videos:    ");
        resultListDisplay = new List(10);
        resultListDisplay.setPreferredSize(new Dimension(0,200));
        resultListDisplay.addActionListener(this);
        queryPanel.add(matchLabel);
        queryPanel.add(resultListDisplay);

        //Result Panel
        Panel resultPanel = new Panel();
        resultPanel.setPreferredSize(new Dimension(1200,300));

        // Visual Slider
        visualSlider = new JSlider(0, 480, 0);
        // paint the ticks and tracks
        visualSlider.setPaintTrack(true);
        visualSlider.setPaintTicks(true);
        visualSlider.setPaintLabels(true);
        // set spacing
        visualSlider.setMajorTickSpacing(50);
        visualSlider.setMinorTickSpacing(5);
        visualSlider.setPreferredSize(new Dimension(600,50));
        visualSlider.addChangeListener(this);

        // Drawing the visual descriptor
        ArrayList<Double> scores = new ArrayList<Double>();
        Random random = new Random();
        int maxDataPoints = 480;
        int maxScore = 1;
        for (int i = 0; i < maxDataPoints ; i++) {
            scores.add(Double.valueOf(0));
        }
        drawGraph = new DrawGraph(scores);
        drawGraph.setPreferredSize(new Dimension(600, 300));
        resultPanel.add(drawGraph);
        
        resultPanel.add(visualSlider);
        controlQueryPanel.add(queryPanel);
        controlQueryPanel.add(resultPanel);


        //Video List Panel
        // Contains the videos for both query and matched video.
        Panel listPanel = new Panel();
        listPanel.setLayout(new GridLayout(2, 0));

        this.imageLabel = new JLabel();
        this.resultImageLabel = new JLabel();
        Panel imagePanel = new Panel();
        imagePanel.add(this.imageLabel);
        Panel resultImagePanel = new Panel();
        resultImagePanel.add(this.resultImageLabel);
        listPanel.add(imagePanel);
        listPanel.add(resultImagePanel);

        //Control Panel
        Panel controlPanel = new Panel();
        Panel resultControlPanel = new Panel();

        playButton = new Button("PLAY");
        playButton.addActionListener(this);
        resultPlayButton = new Button("PLAY");
        resultPlayButton.addActionListener(this);
        controlPanel.add(playButton);
        resultControlPanel.add(resultPlayButton);

        pauseButton = new Button("PAUSE");
        pauseButton.addActionListener(this);
        resultPauseButton = new Button("PAUSE");
        resultPauseButton.addActionListener(this);
        controlPanel.add(pauseButton);
        resultControlPanel.add(resultPauseButton);

        stopButton = new Button("STOP");
        stopButton.addActionListener(this);
        resultStopButton = new Button("STOP");
        resultStopButton.addActionListener(this);
        controlPanel.add(stopButton);
        resultControlPanel.add(resultStopButton);
        resultControlPanel.add(errorLabel);
        resultControlPanel.add(frameLabel);

        listPanel.add(controlPanel);
        listPanel.add(resultControlPanel);

        panel = new JPanel();
        panel.add(controlQueryPanel,BorderLayout.NORTH);
        panel.add(listPanel,BorderLayout.CENTER);
        JPanel frameNumPanel = new JPanel();
        frameNumLabel = new JLabel("");
        frameNumLabel.setText("Frame number:0");
        frameNumPanel.add(frameNumLabel);
        panel.add(frameNumPanel, BorderLayout.SOUTH);

        frame.add(panel);

        frame.setSize(1500, 1000);
        frame.setTitle("Find similar videos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    public void showUI() {
        pack();
        // setLocationRelativeTo(null);
        setVisible(true);

    }

    private void playVideo() {
        playingThread = new Thread() {
            public void run() {
                playButton.setEnabled(false);
                System.out.println("Start playing video");
                for (int i = currentFrameNum; i < totalFrameNum; i++) {
                    imageLabel.setIcon(new ImageIcon(images.get(i)));
                    try {
                        sleep(1000/24);
                    } catch (InterruptedException e) {
                        if(playStatus == 3) {
                            currentFrameNum = 0;
                        } else {
                            currentFrameNum = i;
                        }
                        imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));
                        currentThread().interrupt();
                        break;
                    }
                }
                if(playStatus < 2) {
                    playStatus = 3;
                    currentFrameNum = 0;
                    playSound.stop();
                }
                System.out.println("End playing video:");
                playButton.setEnabled(true);
                pauseButton.setEnabled(true);
                stopButton.setEnabled(true);
            }
        };
        audioThread = new Thread() {
            public void run() {
                try {
                    playSound.play();
                } catch (PlayWaveException e) {
                    e.printStackTrace();
                    errorLabel.setText(e.getMessage());
                    return;
                }
            }
        };
        audioThread.start();
        playingThread.start();
    }

    private void playDBVideo() {
        playingDBThread = new Thread() {
            public void run() {
                resultPlayButton.setEnabled(true);
                System.out.println("Start playing db video");
                System.out.println(dbImages.size());
                for (int i = currentDBFrameNum; i < totalFrameNum; i++) {
                    visualSlider.setValue(i);
                    resultImageLabel.setIcon(new ImageIcon(dbImages.get(i)));
                    try {
                        sleep(1000/24);
                    } catch (InterruptedException e) {
                        if(resultPlayStatus == 3) {
                            currentDBFrameNum = 0;
                        } else {
                            currentDBFrameNum = i;
                        }
                        resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));
                        currentThread().interrupt();
                        break;
                    }
                }
                if(resultPlayStatus < 2) {
                    resultPlayStatus = 3;
                    currentDBFrameNum = 0;
                    playDBSound.stop();
                }
                System.out.println("End playing db video:");
                resultPlayButton.setEnabled(true);
                resultPauseButton.setEnabled(true);
                resultStopButton.setEnabled(true);
            }
        };
        audioDBThread = new Thread() {
         public void run() {
         try {
         playDBSound.play();
         } catch (PlayWaveException e) {
         e.printStackTrace();
         errorLabel.setText(e.getMessage());
         return;
         }
         }
         };
        audioDBThread.start();
        System.out.println("Calling the thread here");
        playingDBThread.start();
    }


    private void stopVideo() {
        if(playingThread != null){
            playButton.setEnabled(true);
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
    		playingThread.interrupt();
			audioThread.interrupt();
			playSound.stop();
			playingThread = null;
			audioThread = null;
    	}else{
    		currentFrameNum = 0;
			displayScreenShot();
    	}
    }

    private void stopDBVideo() {
           if(playingDBThread != null){
            resultPlayButton.setEnabled(true);
            resultStopButton.setEnabled(false);
            resultPauseButton.setEnabled(false);
            visualSlider.setValue(0);
            frameNumLabel.setText("Frame number:"+visualSlider.getValue());
    		playingDBThread.interrupt();
			audioDBThread.interrupt();
			playDBSound.stop();
			playingDBThread = null;
			audioDBThread = null;
    	}else{
    		currentDBFrameNum = 0;
			displayDBScreenShot();
    	}
    }

    private void pauseVideo() throws InterruptedException {
        if(playingThread != null) {
            playButton.setEnabled(true);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(false);
            playingThread.interrupt();
            audioThread.interrupt();
            playSound.pause();
            playingThread = null;
            audioThread = null;
        }
    }

    private void pauseDBVideo() throws InterruptedException {
        if(playingDBThread != null){
            resultPlayButton.setEnabled(true);
            resultStopButton.setEnabled(true);
            resultPauseButton.setEnabled(false);
            playingDBThread.interrupt();
            audioDBThread.interrupt();
            playDBSound.pause();
            playingDBThread = null;
            audioDBThread = null;
        }
    }

    private void displayScreenShot() {
        Thread initThread = new Thread() {
            public void run() {
                imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));
            }
        };
        initThread.start();
    }

    private void displayDBScreenShot() {
        Thread initThread = new Thread() {
            public void run() {
                resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));
            }
        };
        initThread.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.playButton) {
            System.out.println("play_query clicked");
            if(this.playStatus > 1) {
                this.playStatus = 1;
                this.playVideo();
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
                stopButton.setEnabled(true);

            }
        } else if(e.getSource() == this.resultPlayButton) {
            System.out.println("resultDB_play clicked");
            if(this.resultPlayStatus > 1) {
                this.resultPlayStatus = 1;
                resultPlayButton.setEnabled(false);
                resultPauseButton.setEnabled(true);
                resultStopButton.setEnabled(true);
                this.playDBVideo();
            }
        } else if(e.getSource() == this.resultPauseButton) {
            System.out.println("resultDB_pause clicked");
            if(this.resultPlayStatus == 1) {
                this.resultPlayStatus = 2;
                try {
                    resultPauseButton.setEnabled(false);
                    resultPlayButton.setEnabled(true);
                    resultStopButton.setEnabled(true);
                    this.pauseDBVideo();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        } else if(e.getSource() == this.pauseButton) {
            System.out.println("pause_query clicked");
            if(this.playStatus == 1) {
                this.playStatus = 2;
                try {
                    pauseButton.setEnabled(false);
                    playButton.setEnabled(true);
                    stopButton.setEnabled(true);
                    this.pauseVideo();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        } else if(e.getSource() == this.stopButton) {
            System.out.println("stop_query clicked");
            if(this.playStatus < 3) {
                this.playStatus = 3;
                stopButton.setEnabled(false);
                pauseButton.setEnabled(true);
                playButton.setEnabled(true);
                this.stopVideo();
            }
        } else if(e.getSource() == this.resultStopButton) {
            System.out.println("resultDB_stop clicked");
            if(this.resultPlayStatus < 3) {
                this.resultPlayStatus = 3;
                resultStopButton.setEnabled(false);
                resultPlayButton.setEnabled(true);
                resultPauseButton.setEnabled(true);
                this.stopDBVideo();
            }
        }
        else if(e.getSource() == this.queryField) {
            String userInput = queryField.getText();
            if(userInput != null && !userInput.isEmpty()) {
                this.playingThread = null;
                this.audioThread = null;
                System.out.println("listener called");
                this.loadVideo(userInput.trim());
                playButton.setEnabled(true);
                pauseButton.setEnabled(true);
                stopButton.setEnabled(true);
            }
        }
        else if(e.getSource() == this.searchButton) {
            resultListDisplay.removeAll();
            System.out.println("Searching for the Best Match");
            queryCount++;
            String userInput = queryField.getText();
            this.playingThread = null;
            this.audioThread = null;
            this.loadVideo(userInput.trim());
            playButton.setEnabled(true);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            RankVideos rv = new RankVideos();
            rv.getQueryDescriptors(userInput);
            sortedIndices = rv.compareDescriptors();
            sumDiff = rv.getSumDiff();
            double[] completeVideoDiff = rv.getCompleteVideoDiff();
            System.out.println("complete diff="+Arrays.toString(completeVideoDiff));

            System.out.println("sorted indices: "+Arrays.toString(sortedIndices));
            System.out.println("percent: "+ Arrays.toString(completeVideoDiff));
            for(int i=0;i<this.videoCount;i++){
                int ind = sortedIndices[i];
                resultListDisplay.add(displayFileNames[ind]+" "+((1-completeVideoDiff[ind])*100)+"%");
            }
            try {
                    this.loadDBVideo(sortedIndices[0]);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

        }

        else if(e.getSource() == this.resultListDisplay) {
            int userSelect = resultListDisplay.getSelectedIndex() ;
            visualSlider.setValue(0);
            System.out.println("Selected Index"+userSelect);
            if(userSelect >= 0) {
                this.stopDBVideo();
                this.playingDBThread = null;
                this.audioDBThread = null;
                try {
                    this.loadDBVideo(sortedIndices[userSelect]);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }


    }
    static String stripExtension (String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }


    private void loadVideo(String userInput) {
        System.out.println("Load query video.");
        try {
            System.out.println("load video");
            if(userInput == null || userInput.isEmpty()){
                return;
            }
            String folderName = fileFolder+"/"+stripExtension(userInput);
            File dbFile = new File(folderName);
            File[] listOfFrames = dbFile.listFiles((dir, name) -> !name.equals(".DS_Store"));
        
            Arrays.sort(listOfFrames, new Comparator<File>() {
    public int compare(File f1, File f2) {
        try {
            int i1 = Integer.parseInt((stripExtension(f1.getName())).replaceAll("[^\\d.]", ""));
            int i2 = Integer.parseInt((stripExtension(f2.getName())).replaceAll("[^\\d.]", ""));
            return i1 - i2;
        } catch(NumberFormatException e) {
            throw new AssertionError(e);
        }
    }
});
            images = new ArrayList<BufferedImage>();

            for(int i=0; i<480; i++) {
               
                InputStream is = new FileInputStream(listOfFrames[i]);
                

                long len = listOfFrames[i].length();
                byte[] bytes = new byte[(int)len];
                int offset = 0;
                int numRead = 0;
                while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                    offset += numRead;
                }
               
                int index = 0;
                BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < HEIGHT; y++) {
                    for (int x = 0; x < WIDTH; x++) {
                        byte r = bytes[index];
                        byte g = bytes[index+HEIGHT*WIDTH];
                        byte b = bytes[index+HEIGHT*WIDTH*2];
                        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                        image.setRGB(x,y,pix);
                        index++;
                    }
                }
                images.add(image);
                is.close();
                playSound = new PlaySound(fileFolder+"/"+stripExtension(userInput)+".wav");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText(e.getMessage());
        }

        this.playStatus = 3;
        currentFrameNum = 0;
        displayScreenShot();
        System.out.println("Close loading query video.");
    }

    private void loadDBVideo(int videoIndex) throws IOException {
        //videoindex get sortedindex value
        System.out.println("Begin loading db video.");
        dbImages = new ArrayList<BufferedImage>(this.totalFrameNum);

        File file = new File(videoFiles[videoIndex]);
        File[] frameList = file.listFiles((dir, name) -> !name.equals(".DS_Store"));
        Arrays.sort(frameList, new Comparator<File>() {
    public int compare(File f1, File f2) {
        try {
            int i1 = Integer.parseInt((stripExtension(f1.getName())).replaceAll("[^\\d.]", ""));
            int i2 = Integer.parseInt((stripExtension(f2.getName())).replaceAll("[^\\d.]", ""));
            return i1 - i2;
        } catch(NumberFormatException e) {
            throw new AssertionError(e);
        }
    }
});

        for (int i = 0; i < 480; i++) {

            RandomAccessFile raf = new RandomAccessFile(frameList[i], "r");
            raf.seek(0);

            long len = frameList[i].length();
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int index = 0;
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    byte r = bytes[index];
                    byte g = bytes[index + HEIGHT * WIDTH];
                    byte b = bytes[index + HEIGHT * WIDTH * 2];
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    image.setRGB(x, y, pix);
                    index++;
                }
            }
            dbImages.add(image);
            playDBSound = new PlaySound(audioFiles[videoIndex]);
        }
        this.resultPlayStatus = 3;
        currentDBFrameNum = 0;
        displayDBScreenShot();
        ArrayList<Double> scores = new ArrayList<Double>();
        Random random = new Random();
        int maxDataPoints = 480;
        int maxScore = 1;
        for (int i = 0; i < maxDataPoints ; i++) { 
            scores.add(Double.valueOf(sumDiff[videoIndex][i]));
        }
        drawGraph.setScores(scores);
        System.out.println("End loading db video contents.");
        resultPlayButton.setEnabled(true);
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        frameNumLabel.setText("Frame number:"+visualSlider.getValue());
    }
}
