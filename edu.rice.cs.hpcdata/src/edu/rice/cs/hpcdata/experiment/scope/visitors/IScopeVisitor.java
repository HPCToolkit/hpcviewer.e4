package edu.rice.cs.hpcdata.experiment.scope.visitors;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.GroupScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.StatementRangeScope;



public interface IScopeVisitor {
    void visit(LineScope scope, ScopeVisitType vt);
    void visit(StatementRangeScope scope, ScopeVisitType vt);
    void visit(LoopScope scope, ScopeVisitType vt);
    void visit(CallSiteScope scope, ScopeVisitType vt);
    void visit(ProcedureScope scope, ScopeVisitType vt);
    void visit(FileScope scope, ScopeVisitType vt);
    void visit(GroupScope scope, ScopeVisitType vt);
    void visit(LoadModuleScope scope, ScopeVisitType vt);
    void visit(RootScope scope, ScopeVisitType vt);
    void visit(Scope scope, ScopeVisitType vt);
}
