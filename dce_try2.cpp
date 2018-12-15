#define DEBUG_TYPE "mydce"
#include "llvm/Pass.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/InstIterator.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Transforms/Scalar/DCE.h"
#include "llvm/Analysis/TargetLibraryInfo.h"
#include "llvm/Transforms/Scalar.h"
#include "llvm/Transforms/Utils/Local.h"
#include <map>
#include <vector>
using namespace llvm;
using namespace std;

static bool countInstructions(Function &F){
	 std::map<std::string, int> opCounter;
	 errs() << "Function " << F.getName() << '\n';
	 for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
	 	for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
	 		if(opCounter.find(i->getOpcodeName()) == opCounter.end()) {
	 			opCounter[i->getOpcodeName()] = 1;
	 		} else {
	 			opCounter[i->getOpcodeName()] += 1;
	 		}	
	 	}
 	 }
	 std::map <std::string, int>::iterator i = opCounter.begin();
	 std::map <std::string, int>::iterator e = opCounter.end();
	 while (i != e) {
		 errs() << i->first << ": " << i->second << "\n";
		 i++;
	 }
	 errs() << "\n";
	 opCounter.clear();
	 return false;
}

static bool DCE(Function &F){
	bool deadInstructionFound = false;	
	SmallVector<Instruction *, 64> Worklist;
	for (inst_iterator I = inst_begin(F), E=inst_end(F); I != E; ++I){
		if (Instruction *I = dyn_cast<Instruction>(&*I)){
			if (isInstructionTriviallyDead(I)){
				Worklist.push_back(I);
				deadInstructionFound = true;
			}
		}
	}
	while (!Worklist.empty()){
   		
		Instruction *I = Worklist.pop_back_val();
		I->eraseFromParent();
	}
	return deadInstructionFound;

}
namespace {
	 struct MyDCE : public FunctionPass {
		 static char ID;
		 MyDCE() : FunctionPass(ID) {}
		 virtual bool runOnFunction(Function &F) {
			//at boundary nobody can be alive
			    virtual void setBoundaryCondition(BitVector *blockBoundary) {
				*blockBoundary = BitVector(domainSize, true); 
			    }

			    //take the intersection. It's a must analysis. we are removing stuff here, better be double sure.
			    virtual void meetOp(BitVector* lhs, const BitVector* rhs){
				*lhs &= *rhs; 
			    }

			    //be optimistic. Assume variable is dead initially
			    virtual BitVector* initializeFlowValue(BasicBlock& b, SetType setType){ 
				return new BitVector(domainSize, true); 
			    }


			    //transfer function:
			    //IN[n] = USE[n] U (OUT[n] - DEF[n]) //transfer function

			    virtual BitVector* transferFn(BasicBlock& bb) {
				BitVector* outNowIn = new BitVector(*((*out)[&bb]));

				BitVector* immIn = outNowIn; // for empty blocks
				Instruction* tempInst;
				bool breakme=false;
				// go through instructions in reverse
				BasicBlock::iterator ii = --(bb.end()), ib = bb.begin();
				while (true) {

				    // inherit data from next instruction
				    tempInst = &*ii;
				    immIn = (*instrInSet)[tempInst];            
				    *immIn = *outNowIn;

				    if (!isDefinition(tempInst) || !(*immIn)[(*valueToBitVectorIndex)[tempInst]]) 
				    {
				        User::op_iterator OI, OE;
				        for (OI = tempInst->op_begin(), OE=tempInst->op_end(); OI != OE; ++OI) 
				        {
				            if (isa<Instruction>(*OI) || isa<Argument>(*OI)) 
				            {
				                (*immIn)[(*valueToBitVectorIndex)[*OI]] = false;
				            }
				        }
				    }

				    outNowIn = immIn;

				    if (ii == ib) break;

				    --ii;
				}

				return immIn;
			    }

			    bool isDefinition(Instruction *ii) {
				return !(isa<TerminatorInst>(ii) || isa<CallInst>(ii)) ;
			    }
				 
		 }
	 };
}
char MyDCE::ID = 0;
static RegisterPass<MyDCE> X("mydce", "Eliminates dead code and counts opcodes per functions");
