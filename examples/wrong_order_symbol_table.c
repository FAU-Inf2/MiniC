int foo;

void bar() {
  int foo;
  foo = 13;
}

int main() {
  bar();
  print(foo);
  return 0;
}
