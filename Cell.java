import java.util.Random;

class Cell{
    int cellCycleLength;
    int positionInCellCycle;
    boolean brafRes;
    boolean PDL1;
    boolean pR;
//    public Neoantigens neoAntigenLoad;
    int[] neoAntigenLoad = new int[Fantastasize.totalNA];
    boolean Alive;


    public Cell(int[] neos, int ccLength){//constructor
        this.positionInCellCycle=0;
        this.cellCycleLength=ccLength;
        this.inheritNeos(neos);
        this.brafRes = getBRAFresStat();
        this.PDL1 = getPDL1Stat();
        this.pR = getpR();
        this.Alive = true;
    }

    public void setInititialCondition(){
        this.PDL1=false;
        this.brafRes=false;
        this.pR=false;
//        this.neoAntigenLoad[Cell.generator.nextInt(Fantastasize.numNA)]=1;
    }

    public void inheritNeos(int[] neos){
        for (int i = 0; i < Fantastasize.totalNA; i++) {
            if( neos[i]==1){
                neoAntigenLoad[i]=neos[i];
            }
        }
    }

    public void advance() {
        positionInCellCycle++;}

    public boolean isMature(){
        return positionInCellCycle>=this.cellCycleLength; }

    public int getPositionInCellCycle(){
        return this.positionInCellCycle; }

    public int getCellCycleLength(){
        return this.cellCycleLength; }

    public boolean getBRAFresStat(){
        return this.brafRes; }

    public boolean getPDL1Stat(){
        return this.PDL1; }

    public boolean getpR(){
        return this.pR; }

    public void resetCCCycler(){
        this.positionInCellCycle=0;
    }

    public int[] immunogenicNeos(){
        int[] immuno = new int[Fantastasize.totalImmuno];
        for (int i = 0; i < Fantastasize.totalImmuno; i++) {
            if ( neoAntigenLoad[i] == 1 ) {
                immuno[i] = 1;
            } else {
                immuno[i] = 0;
            }
        }
        return(immuno);
    }

    public void proteomemutate(){
        if(Fantastasize.generator.nextInt(Fantastasize.neoMutRate)==0){//random roll - neoAntigen accumulation
            int NAidx = Fantastasize.generator.nextInt(Fantastasize.totalNA);
            this.neoAntigenLoad[NAidx]=1;
        }
    }

    public void phenomute(){
//        if(Fantastasize.generator.nextInt(Fantastasize.mutRate)==0){//mutation rate
//            int whichMut = Fantastasize.generator.nextInt(3);
//            if(whichMut==0){
//                this.cellCycleLength=maxmin[0];
//                this.pR = true;
//            }
//            else if(whichMut==1){
//                this.brafRes=getBRAFresStat();
//            }
//            else{
//                this.PDL1=getPDL1Stat();
//            }
//
//        }
    }
};