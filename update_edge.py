import os, glob

activities = [
    'app/src/main/java/com/example/midtermproject/ui/user/UserMainActivity.java',
    'app/src/main/java/com/example/midtermproject/ui/admin/AdminMainActivity.java'
]

for filepath in activities:
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
            if (binding.bottomNavigation != null) {
                binding.bottomNavigation.setPadding(0, 0, 0, insets.bottom);
            }
            return windowInsets;
        });
"""
        # AdminMainActivity has admin_bottom_navigation
        if 'AdminMainActivity' in filepath:
            logic = logic.replace('binding.bottomNavigation', 'binding.adminBottomNavigation')
            
        content = content.replace('setContentView(binding.getRoot());', 'setContentView(binding.getRoot());\n' + logic)
        
        with open(filepath, 'w') as f:
            f.write(content)
            
