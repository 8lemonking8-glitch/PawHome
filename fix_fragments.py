import os

fragments = [
    ('app/src/main/java/com/example/midtermproject/ui/profile/ProfileFragment.java', 'binding.ivAvatar'),
    ('app/src/main/java/com/example/midtermproject/ui/admin/AdminDashboardFragment.java', 'binding.btnLogout')
]

for filepath, view_target in fragments:
    if not os.path.exists(filepath):
        continue
    with open(filepath, 'r') as f:
        content = f.read()

    # First, remove the incorrect ViewCompat block that used binding.tvGreeting
    import re
    # The previous script added:
    #         ViewCompat.setOnApplyWindowInsetsListener(binding.tvGreeting, (v, windowInsets) -> { ... });
    content = re.sub(r'        ViewCompat\.setOnApplyWindowInsetsListener\(binding\.tvGreeting.*?\n        \}\);\n', '', content, flags=re.DOTALL)
    
    # Now add the correct block
    logic = """
        ViewCompat.setOnApplyWindowInsetsListener(""" + view_target + """, (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            android.view.View targetView = """ + ('v' if 'ivAvatar' in view_target else '(android.view.View) v.getParent()') + """;
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
            mlp.topMargin = insets.top;
            targetView.setLayoutParams(mlp);
            return windowInsets;
        });
"""
    content = content.replace('super.onViewCreated(view, savedInstanceState);', 'super.onViewCreated(view, savedInstanceState);\n' + logic)
    
    with open(filepath, 'w') as f:
        f.write(content)

