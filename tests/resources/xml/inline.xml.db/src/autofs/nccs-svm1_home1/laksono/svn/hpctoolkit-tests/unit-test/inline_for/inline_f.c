

#define ITER_OUT 1<<14
#define ITER_IN  1<<16

__attribute__((always_inline))
long 
f(int stop)
{
  int i, j, sum = stop;

#include "par_forloop.h"
  for (i=0; i<ITER_OUT; i++) {
    for (j=ITER_IN; j>0; j--) {
      sum += stop +  i + j;
    }
  }
  return sum;
}
