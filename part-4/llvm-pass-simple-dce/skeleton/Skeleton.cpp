#define DEBUG_TYPE "opCounter"
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
	 int x;
	 //errs() << "Function " << F.getName() << '\n';
	 for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
	 	for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
			//x = i->getNumOperands();
			//errs() << i->getOpcodeName() << " with number of operands ====> " << x << "\n";
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
	 //errs() << "\n";
	 opCounter.clear();
	 return false;
}

static bool DCE(Function &F){
	bool deadInstructionFound = false;	
	SmallVector<Instruction *, 64> Worklist;
	// Aaron said opposite approach so
	// Put all instructions in the Worklist
	for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
	 	for (BasicBlock::iterator i = bb->end(), e = bb->begin(); i != e; --i) {
			Worklist.push_back(&*i);
		}
	}
	while (!Worklist.empty()) {
		// Use Instruction from the bottom of stack
    		Instruction *I = Worklist.back();
    		Worklist.pop_back();
		
    		if (isInstructionTriviallyDead(I)) { 
	      		// Iterate over the values of the Instruction and push the used Instructions on Worklist
	      		for (User::op_iterator op_i = I->op_begin(), op_e = I->op_end(); op_i != op_e; ++op_i) {
				if (Instruction *used_instr = dyn_cast<Instruction>(*op_i)) {
		  			Worklist.push_back(used_instr);
				}
			}
	      		// Remove and erase the Instruction
	      		I->eraseFromParent();
	      		Worklist.erase(std::remove(Worklist.begin(), Worklist.end(), I), Worklist.end());

	      		deadInstructionFound = true;
    		}
  	}
	return deadInstructionFound;
}
namespace {
   struct SimpleDCE : public FunctionPass {
     static char ID;
     SimpleDCE() : FunctionPass(ID) {}
     
     
     virtual bool runOnFunction(Function &F) {
       errs() << "BEFORE DCE\n";
       //Count before dead code elimination here 
       countInstructions(F);
       errs() << "DCE START\n";
       //Eliminate dead code and produce counts here
       DCE(F);
       countInstructions(F);
       errs() << "DCE END\n";
       return false;
     }
   };
}
char SimpleDCE::ID = 0;
static RegisterPass<SimpleDCE> X("simpledce", "Simple dead code elimination");

