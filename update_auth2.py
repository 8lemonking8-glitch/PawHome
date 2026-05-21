import os

filepath = 'app/src/main/java/com/example/midtermproject/ui/auth/AuthActivity.java'
with open(filepath, 'r') as f:
    content = f.read()

switch_tab = """    public void switchToLoginTab() {
        binding.viewPager.setCurrentItem(0, true);
    }"""
content = content.replace('    public void switchToRegisterTab() {', switch_tab + '\n\n    public void switchToRegisterTab() {')

with open(filepath, 'w') as f:
    f.write(content)

filepath = 'app/src/main/java/com/example/midtermproject/ui/auth/RegisterFragment.java'
with open(filepath, 'r') as f:
    content = f.read()

click_listener = """        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.cardLoginLink.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).switchToLoginTab();
            }
        });"""

content = content.replace('        binding.btnRegister.setOnClickListener(v -> attemptRegister());', click_listener)

with open(filepath, 'w') as f:
    f.write(content)

