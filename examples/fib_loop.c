int fib(int n) {
  int a;
  int b;
  int i;

  a = 1;
  b = 1;
  i = 2;

  while (i < n) {
    int t;
    t = a + b;
    a = b;
    b = t;
    i = i + 1;
  }

  return b;
}

int main() {
  return fib(8);
}
