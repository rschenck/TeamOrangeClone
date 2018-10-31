import com.sun.tools.javac.util.ArrayUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class Fantastasize
{
    public static int burstsize = 10;
	public static int totalNA = 1000;
	public static int totalImmuno = 10;
	public static int TCRdelay = 4;
	public static int[] immunoTracker = new int[totalImmuno+totalImmuno*TCRdelay]; // Immunogenic clone population tracker
    public static int[] immunoCurrent = new int[totalImmuno]; // Current timestep sum of immunogenic cells
	public static int[] TCRpop = new int[totalImmuno];
	public static int[] TCRinactive = new int[totalImmuno];
	public static double inactivateRatePDL1 = 20;
	public static int totalPDL1;
	public static int TKillRate = 1;
	public static int mutRate = 400;
	public static boolean immunoTxOn = false;
	public static int immunoTxTime = 20*24;

    static int[] ccmaxmin={10,40};//high and low of cell cycle values

    static Random generator = new Random(); //random number generator
    static int neoMutRate = 20;//mut 1/20

    public static void main (String[] args)
	{
		//set initial condition for TCR
        for (int i=0;i<totalImmuno;i++){
            TCRpop[i]=1;
            TCRinactive[i]=0;//set inactive TCR to zero initially
        }

		ArrayList<Cell> cellList = new ArrayList<Cell>();
        ArrayList<Cell> AliveList = new ArrayList<Cell>();


        // Cell Initilization
        Neoantigens firstneos = new Neoantigens();
        burstInitializer(firstneos);
        cellList.add(new Cell(new int[Fantastasize.totalNA],ccmaxmin[1]));
		Cell initialCell = cellList.get(0);
		initialCell.setInititialCondition();

        int tmax=365*24+1; // Run Time
        for (int t=1; t< tmax; t++){//time loop
            immunoTxOn=(t==immunoTxTime)?true:false;
			int cellListSize=cellList.size();
			totalPDL1=0;
			for (int i=0; i<cellListSize; i++){//cell loop
			    Cell cell = cellList.get(i);
                totalPDL1+=(cell.PDL1)?1:0;
				cell.advance();

				// Births
				if (cell.isMature()){
                    cell.proteomemutate(); // Acquire neoantigens
                    cell.phenomute(); // Mutate phenotype
					Cell newC = new Cell(cell.neoAntigenLoad,cell.cellCycleLength);
					cellList.add(newC);
                    cell.resetCCCycler(); // Reset Cell Cycle Cycler
//                    System.out.println(cell.cellCycleLength+" "+newC.cellCycleLength);
				}

				// Immunogenic Clone Track This Timestep:
                int[] immunogenicity = cell.immunogenicNeos();
                for (int j = 0; j < totalImmuno; j++) {
                    immunoCurrent[j]+=immunogenicity[j];
                }

			}
            UpdateCloneTracker(t); // Update and move for previous days

            // TCR Delayed Birth Rate
            int totalSize = 0;
            for (int i = 0; i < TCRpop.length; i++) {
                if(TCRpop[i]>1){
                    TCRpop[i] = (int) Math.round(TCRpop[i]*0.85);//TCR death
                    TCRpop[i] += Math.pow(immunoTracker[i],1.1);//TCR birth
                    double tempThis = (immunoTxOn && TCRpop[i]>0) ? -inactivateRatePDL1*(TCRinactive[i])*totalPDL1 :inactivateRatePDL1*(TCRpop[i]-TCRinactive[i])*totalPDL1;
                    TCRinactive[i] += tempThis/(cellListSize+0.f);
                } else{
                    TCRpop[i] += Math.pow(immunoTracker[i],1.1);//no death if below 1
                }
                totalSize += immunoCurrent[i] + TCRpop[i];
            }

            // Cell Death
            for (int i = 0; i < cellListSize; i++) {
                Cell cell = cellList.get(i);

                ArrayList<Integer> immunoPresence = new ArrayList<>();
                int[] cellImp = cell.immunogenicNeos();
                for (int j = 0; j < totalImmuno; j++) {
                    if (cellImp[j] == 1) {
                        immunoPresence.add(j); // Collects immunogenic epitopes
                    }
                }

                if(immunoPresence.size()>0 && t>TCRdelay){

                    double DeathProb = 0.;
                    for (int j = 0; j < immunoPresence.size(); j++) {
                        double numerator =  (TCRpop[immunoPresence.get(j)]-TCRinactive[immunoPresence.get(j)]) * immunoCurrent[immunoPresence.get(j)];
                        double denominator = totalSize;
                        DeathProb += 0.4*numerator/denominator;
                    }
//                    System.out.println(DeathProb);

                    cellList.size();
                    if( DeathProb > generator.nextDouble()) {

                        cell.Alive=false;

                        if(generator.nextInt(5)==0){
                            int toKill = generator.nextInt(immunoPresence.size());
                              if(TCRpop[immunoPresence.get(toKill)]>1){
                                TCRpop[immunoPresence.get(toKill)]-=1;
                            }
                        }
//                        int toKill = generator.nextInt(immunoPresence.size());
//                        if(TCRpop[immunoPresence.get(toKill)]>1){
//                            TCRpop[immunoPresence.get(toKill)]-=1;
//                        }
                    }

                }

            }

            //TODO update list with alive cells
            for (int j = 0; j < cellList.size(); j++) {
                if(cellList.get(j).Alive){
                    AliveList.add(cellList.get(j));
                }
            }
            cellList=new ArrayList<Cell>();
            cellList=AliveList;
            AliveList=new ArrayList<Cell>();
            System.gc();
            System.out.println(t + "\t" + Arrays.toString(TCRpop) + "\t" + Arrays.toString(immunoCurrent) + "\t" + cellListSize + "\t" + Arrays.toString(TCRinactive)+ "\t" +totalPDL1);

            ResetCurrentStepImmunoTracker();

		}
	}

	public static void write (String filename, String data) {
		try{//exception handling, print a message if doesn't succeed
    		FileWriter fw = new FileWriter(filename,true); //the true will append the new data
    		fw.write(data);//appends the string to the file
   	 		fw.close();
		}
		catch(IOException ioe){
    		System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static void UpdateCloneTracker(int t){
        int[] updateTracker = new int[immunoTracker.length];
        // Step 1: Copy all but the current immunoClone slots
        for (int i = totalImmuno; i < immunoTracker.length; i++) {
            updateTracker[i-totalImmuno] = immunoTracker[i];
        }

        // Step 2: Add the Current timestep to the array in the first immunoClone slots
        for (int i = 0; i < totalImmuno; i++) {
            updateTracker[i+totalImmuno*TCRdelay] = immunoCurrent[i];
        }
        immunoTracker = updateTracker;
    }

	public static void ResetCurrentStepImmunoTracker(){
        immunoCurrent=new int[totalImmuno];
    }

	public static void burstInitializer(Neoantigens firstneos) {
        for (int i = 0; i < burstsize; i++) {
            int NAidx = Fantastasize.generator.nextInt(Fantastasize.totalNA);
            firstneos.set(NAidx);
        }
    }


}
