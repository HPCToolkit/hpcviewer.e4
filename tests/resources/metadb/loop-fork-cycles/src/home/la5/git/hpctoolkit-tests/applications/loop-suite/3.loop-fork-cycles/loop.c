#include "../include/common.h"

#include <sys/types.h>
#include <unistd.h>

void parent(long n)
{
  loop(n);
}

void child(long n)
{
  loop(n);
}

void test(long n)
{
  if (fork()) { 
    parent(n);
  } else {
    child(n);
  }
}
