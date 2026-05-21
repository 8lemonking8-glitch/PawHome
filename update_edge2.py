import os

filepath = 'app/src/main/java/com/example/midtermproject/ui/auth/AuthActivity.java'
with open(filepath, 'r') as f:
    content = f.read()

if 'WindowCompat.setDecorFitsSystemWindows' not in content:
    imports = """
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
"""
    content = content.replace('import android.os.Bundle;', 'import android.os.Bundle;\n' + imports)
    
    logic = """
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, insets.bottom);
            // Add top margin to logo
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) binding.ivLogo.getLayoutParams();
            mlp.topMargin = insets.top + (int)(40 * getResources().getDisplayMetrics().density);
            binding.ivLogo.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });
"""
    content = content.replace('setContentView(binding.getRoot());', 'setContentView(binding.getRoot());\n' + logic)
    with open(filepath, 'w') as f:
        f.write(content)

filepath = 'app/src/main/java/com/example/midtermproject/ui/detail/PetDetailActivity.java'
with open(filepath, 'r') as f:
    content = f.read()

if 'WindowCompat.setDecorFitsSystemWindows' not in content:
    imports = """
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
"""
    content = content.replace('import android.os.Bundle;', 'import android.os.Bundle;\n' + imports)
    
    logic = """
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams();
            mlp.topMargin = insets.top;
            binding.toolbar.setLayoutParams(mlp);
            
            binding.fabAdopt.setPadding(0, 0, 0, insets.bottom);
            
            return WindowInsetsCompat.CONSUMED;
        });
"""
    content = content.replace('setContentView(binding.getRoot());', 'setContentView(binding.getRoot());\n' + logic)
    with open(filepath, 'w') as f:
        f.write(content)

