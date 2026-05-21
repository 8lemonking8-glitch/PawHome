import os

fragments = [
    ('app/src/main/java/com/example/midtermproject/ui/home/HomeFragment.java', 'binding.tvGreeting'),
    ('app/src/main/java/com/example/midtermproject/ui/profile/ProfileFragment.java', 'binding.tvGreeting'),
    ('app/src/main/java/com/example/midtermproject/ui/admin/AdminDashboardFragment.java', 'binding.tvGreeting')
]

for filepath, view_target in fragments:
    if not os.path.exists(filepath):
        continue
    with open(filepath, 'r') as f:
        content = f.read()

    if 'ViewCompat.setOnApplyWindowInsetsListener' not in content:
        imports = """
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
"""
        content = content.replace('import android.os.Bundle;', 'import android.os.Bundle;\n' + imports)
        
        logic = """
        ViewCompat.setOnApplyWindowInsetsListener(""" + view_target + """, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);
            return windowInsets;
        });
"""
        # Place it right after super.onViewCreated
        content = content.replace('super.onViewCreated(view, savedInstanceState);', 'super.onViewCreated(view, savedInstanceState);\n' + logic)
        
        with open(filepath, 'w') as f:
            f.write(content)

