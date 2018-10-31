import java.util.Arrays;

public class Neoantigens {
    private int[] neoAntigenLoad = new int[Fantastasize.totalNA];

    public int get(int i){
        return(neoAntigenLoad[i]);
    }

    public void set(int idx){
        neoAntigenLoad[idx]=1;
    }

    public int getNeoLoadLength(){
        return(neoAntigenLoad.length);
    }

    public int[] immunogenicNeos(){
        int[] immuno = new int[Fantastasize.totalImmuno];
        for (int i = 0; i < Fantastasize.totalImmuno; i++) {
            if ( get(neoAntigenLoad[i]) == 1 ) {
                immuno[i] = 1;
            } else {
                immuno[i] = 0;
            }
        }
        return(immuno);
    }

    public void printArray(){
        System.out.println(Arrays.toString(neoAntigenLoad));
    }
}
