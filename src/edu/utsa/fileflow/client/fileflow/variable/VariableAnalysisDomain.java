package edu.utsa.fileflow.client.fileflow.variable;

import edu.utsa.fileflow.analysis.AnalysisDomain;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Rodney on 2/11/2017.
 * <p>
 * This class is analysis domain for variable analysis. It holds a grammar which
 * will track all productions and live variables in the entire program.
 */
public class VariableAnalysisDomain extends AnalysisDomain<VariableAnalysisDomain> {

    VariableGrammar grammar = new VariableGrammar();
    Set<Variable> liveVariables = new HashSet<>();

    @Override
    public VariableAnalysisDomain merge(VariableAnalysisDomain domain) {
        return null;
    }

    @Override
    public VariableAnalysisDomain top() {
        // TODO: implement a top rather than just a new domain
        return new VariableAnalysisDomain();
    }

    @Override
    public VariableAnalysisDomain bottom() {
        VariableAnalysisDomain bottom = new VariableAnalysisDomain();
        bottom.grammar = new VariableGrammar();
        bottom.liveVariables = new HashSet<>();
        return bottom;
    }

    @Override
    public int compareTo(VariableAnalysisDomain o) {
        if (!grammar.equals(o.grammar))
            return 1;
        if (!liveVariables.equals(o.liveVariables))
            return 1;
        return 0;
    }

    @Override
    public VariableAnalysisDomain clone() {
        VariableAnalysisDomain clone = bottom();
        clone.liveVariables.addAll(liveVariables);
        clone.grammar = grammar.clone();
        return clone;
    }

}
