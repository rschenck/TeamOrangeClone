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
    public static int burstsize = 3;
	public static int totalNA = 5;
	public static int totalImmuno = 2;
	public static int TCRdelay = 4;
	public static int[] immunoTracker = new int[totalImmuno+totalImmuno*TCRdelay]; // Immunogenic clone population tracker
    public static int[] immunoCurrent = new int[totalImmuno]; // Current timestep sum of immunogenic cells
	public static int[] TCRpop = new int[totalImmuno];
	public static int TKillRate = 1;
    int tmax=10*24+1;

    static Random generator = new Random(); //random number generator
    static int neoMutRate = 100;//mut 1/1000
    static int mutRate = 1000;

    public static void main (String[] args)
	{
		//set initial condition for TCR
        for (int i=0;i<totalImmuno;i++){
            TCRpop[i]=1;
        }

		ArrayList<Cell> cellList = new ArrayList<Cell>();

		// Cell Initilization
        Neoantigens firstneos = new Neoantigens();
        firstneos.printArray();

        burstInitializer(firstneos);
        cellList.add(new Cell(firstneos));
		Cell initialCell = cellList.get(0);
		initialCell.setInititialCondition();

        int tmax=50; // Run Time
        for (int t=1; t< tmax; t++){//time loop

			int cellListSize=cellList.size();
			for (int i=0; i<cellListSize; i++){//cell loop
			    Cell cell = cellList.get(i);
				cell.advance();

				// Births
				if (cell.isMature()){
					cell.resetCCCycler(); // Reset Cell Cycle Cycler
                    cell.proteomemutate(); // Acquire neoantigens
                    cell.phenomute(); // Mutate phenotype
					Cell newC = new Cell(cell.neoAntigenLoad);
					cellList.add(newC);
				}

				// Immunogenic Clone Track This Timestep:
                int[] immunogenicity = cell.neoAntigenLoad.immunogenicNeos();
                for (int j = 0; j < totalImmuno; j++) {
                    immunoCurrent[j]+=immunogenicity[j];
                }

			}
            UpdateCloneTracker(t); // Update and move for previous days

            // TCR Delayed Birth Rate
            int totalSize = 0;
            for (int i = 0; i < TCRpop.length; i++) {
                if(TCRpop[i]>1){
                    TCRpop[i] += Math.pow(immunoTracker[i],1.2) - TCRpop[i]*0.85;
                } else{
                    TCRpop[i] += Math.pow(immunoTracker[i],1.2);
                }
                totalSize += immunoCurrent[i] + TCRpop[i];
            }

            // Cell Death
//            double[] deathArray = new double[totalImmuno]; // Update with the probabilities of death
//            for (int i = 0; i < totalImmuno; i++) {
//
//                deathArray[i] = totalImmuno
//            }
            // P(Death) =

            for (int i = 0; i < cellListSize; i++) {
                Cell cell = cellList.get(i);

                ArrayList<Integer> immunoPresence = new ArrayList<>();
                int[] cellImp = cell.neoAntigenLoad.immunogenicNeos();
                for (int j = 0; j < totalImmuno; j++) {
                    if (cellImp[j] == 1) {
                        immunoPresence.add(j); // Collects immunogenic epitopes
                    }
                }

                if(immunoPresence.size()>0 && t>TCRdelay){

                    double DeathProb = 0.;
                    for (int j = 0; j < immunoPresence.size(); j++) {
                        double numerator =  TCRpop[immunoPresence.get(j)] + immunoCurrent[immunoPresence.get(j)];
                        double denominator = totalSize;
                        DeathProb += 0.05*numerator/denominator;
                    }
//                    System.out.println(DeathProb);

                    cellList.size();
                    if( DeathProb > generator.nextDouble() ) {

                        cellList.remove(i);

                        int toKill = generator.nextInt(immunoPresence.size());
                        if(TCRpop[immunoPresence.get(toKill)]>1){
                            TCRpop[immunoPresence.get(toKill)]-=1;
                        }
                    }

                }

            }
            ResetCurrentStepImmunoTracker();


            System.out.println(Arrays.toString(TCRpop) + '\t' + Arrays.toString(immunoTracker));



//            if (t%1==0){//print population every day
//            	int sumNA = findTotalArray(totNAL);
//            	int sumTNAL = findTotalArray(TCRpop);
//               System.out.println(t+","+cellList.size()+","+sumNA+","+sumTNAL);
//
//                //write TCR to file
//                String str="";//create an empty string
//                for(int i=0; i<TCRpop.length; i++){
//                    str += " "+TCRpop[i];//record all of TCR to file
//                }
//                str += "\n";//creates a new line;
//                write("TCR.txt",str); //write string to file
//
//                //write NAL to file
//                str="";//create an empty string
//                for(int i=0; i<totNAL.length; i++){
//                    str += " "+totNAL[i];//record all of TCR to file
//                }
//                str += "\n";//creates a new line;
//                write("totNAL.txt",str); //write string to file
//            }

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

	public static void UpdateCloneTracker(int t){
        int[] updateTracker = new int[immunoTracker.length];
        // Step 1: Copy all but the current immunoClone slots
        for (int i = totalImmuno; i < immunoTracker.length; i++) {
            updateTracker[i-totalImmuno] = immunoTracker[i];
        }

        // Step 2: Add the Current timestep to the array in the first immunoClone slots
        for (int i = 0; i < totalImmuno; i++) {
//            System.out.println(i);
//            System.out.println(i+totalImmuno*TCRdelay);
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
