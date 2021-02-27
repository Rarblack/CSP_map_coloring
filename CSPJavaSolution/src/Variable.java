import java.util.HashSet;
import java.util.Set;

class Variable{

    // a color assigned to the variable
    private int color = -1;

    // colors that are assignable to the vertices (i.e domain)
    private final Set<Integer> domain;

    // adjacent vertices connected through edges
    private final Set<Integer> arcs;


    Variable(int colorsRange){
        domain = new HashSet<Integer>();
        for(int i = 0; i < colorsRange; i++){
            domain.add(i);
        }
        arcs = new HashSet<Integer>();
    }

    /**
     * Getter and setter functions for class variables
     */

    public int getColor(){
        return this.color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public Set<Integer> getDomain(){
        return domain;
    }

    public void addDomainValue(int value){
        domain.add(value);
    }

    public void removeDomainValue(int value){
        domain.remove(value);
    }

    public Set<Integer> getArcs(){
        return this.arcs;
    }

    public void addArc(int a){
        arcs.add(a);
    }
}