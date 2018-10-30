import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

class Cell{   
    static int[] maxmin={10,40};//high and low of cell cycle values
    static Random generator = new Random(); //random number generator
    static int neoMutRate = 100;//mut 1/1000
    static int mutRate = 1000;

    int cellCycleLength;
	int positionInCellCycle;
	boolean brafRes;
	boolean PDL1;
	boolean pR;
	int[] neoAntigenLoad = new int[Fantastasize.numNA];
    
	public Cell(){//constructor
	    this.positionInCellCycle=0;
        this.cellCycleLength=getCellCycleLength();
        this.neoAntigenLoad=getNeoAntigenLoad();
        this.brafRes = getBRAFresStat();
        this.PDL1 = getPDL1Stat();
        this.pR = getpR();
	}

	public void setInititalCondition(){
        this.PDL1=false;
        this.brafRes=false;
        this.pR=false;
        this.neoAntigenLoad[Cell.generator.nextInt(Fantastasize.numNA)]=1;
        this.cellCycleLength=maxmin[1];
    }
    
	public void advance() {
		positionInCellCycle++;}
	
	public boolean isMature(){
		return positionInCellCycle>=this.cellCycleLength; }
	
	public int getPositionInCellCycle(){
		return this.positionInCellCycle; }

	public int getCellCycleLength(){
		return this.cellCycleLength; }

	public int[] getNeoAntigenLoad(){
		return this.neoAntigenLoad; }

    public boolean getBRAFresStat(){
        return this.brafRes; }

    public boolean getPDL1Stat(){
        return this.PDL1; }

    public boolean getpR(){
        return this.pR; }
	
	public void reset(){
		this.positionInCellCycle=0;
		if(generator.nextInt(neoMutRate)==0){//random roll - neoAntigen accumulation
			int newNA = generator.nextInt(Fantastasize.numNA);
			this.neoAntigenLoad[newNA]+=1;
			Fantastasize.totNAL[newNA]+=1;
			Fantastasize.TCR[newNA]+=1;
//			System.out.println("hi");
		}
		if(generator.nextInt(mutRate)==0){//mutation rate
		    int whichMut = generator.nextInt(3);
		    if(whichMut==0){
                this.cellCycleLength=maxmin[0];
                this.pR = true;
            }
            else if(whichMut==1){
                this.brafRes=getBRAFresStat();
            }
            else{
                this.PDL1=getPDL1Stat();
            }

		}
	}
};

public class Fantastasize
{	
	public static int numNA = 100;
	public static int[] totNAL = new int[numNA];
	public static int[] TCR = new int[numNA];
	public static int tRate = 1;

	public static void main (String[] args)
	{
		int tmax=10*24+1;

        //set initial condition for TCR
        for (int i=0;i<numNA;i++){
            TCR[i]=1;
        }

		List<Cell> cellList = new ArrayList<Cell>();

		//add new cell
		cellList.add(new Cell());
		Cell cell = cellList.get(0);
		cell.setInititalCondition();

		totNAL[0]=1;
		int cellListSize;

		for (int t=1; t<tmax; t++){//time loop
			cellListSize=cellList.size();
			for (int i=0; i<cellListSize; i++){//cell loop
//			    System.out.println(t);
				cellList.get(i).advance();
				if (cellList.get(i).isMature()){
					cellList.get(i).reset();
					Cell newC = new Cell();
					cellList.add(newC);
				}
			}
            if (t%1==0){//print population every day
            	int sumNA = findTotalArray(totNAL);
            	int sumTNAL = findTotalArray(TCR);
               System.out.println(t+","+cellList.size()+","+sumNA+","+sumTNAL);

                //write TCR to file
                String str="";//create an empty string
                for(int i=0; i<TCR.length; i++){
                    str += " "+TCR[i];//record all of TCR to file
                }
                str += "\n";//creates a new line;
                write("TCR.txt",str); //write string to file

                //write NAL to file
                str="";//create an empty string
                for(int i=0; i<totNAL.length; i++){
                    str += " "+totNAL[i];//record all of TCR to file
                }
                str += "\n";//creates a new line;
                write("totNAL.txt",str); //write string to file
            }

		}
	}

	public static int findTotalArray(int[] array){
		int sum=0;
		for(int i=0;i<array.length;i++){
			sum+=array[i];
		}
		return sum;
	}

	public static void write (String filename, String data)
	{
		try{//exception handling, print a message if doesn't succeed
    		FileWriter fw = new FileWriter(filename,true); //the true will append the new data
    		fw.write(data);//appends the string to the file
   	 		fw.close();
		}
		catch(IOException ioe){
    		System.err.println("IOException: " + ioe.getMessage());
		}
	}


}
