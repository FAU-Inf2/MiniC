int foo;

void bar() {
  int foo;
  foo = 13;
  return;
}

int main() {
  bar();
  print(foo);
  return 0;
}
