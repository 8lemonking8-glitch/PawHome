import os

filepath = 'app/src/main/java/com/example/midtermproject/ui/auth/LoginFragment.java'
with open(filepath, 'r') as f:
    content = f.read()

click_listener = """        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.cardRegisterLink.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).switchToRegisterTab();
            }
        });"""

content = content.replace('        binding.btnLogin.setOnClickListener(v -> attemptLogin());', click_listener)

with open(filepath, 'w') as f:
    f.write(content)

