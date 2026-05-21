import os

fragments = [
    ('app/src/main/java/com/example/midtermproject/ui/favorites/FavoritesFragment.java', 'binding.rvFavorites'),
    ('app/src/main/java/com/example/midtermproject/ui/admin/AdminPetsFragment.java', 'binding.rvPets'),
    ('app/src/main/java/com/example/midtermproject/ui/admin/AdminRequestsFragment.java', 'binding.rvRequests')
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
            v.setPadding(v.getPaddingLeft(), insets.top + (int)(8 * getResources().getDisplayMetrics().density), v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });
"""
        content = content.replace('super.onViewCreated(view, savedInstanceState);', 'super.onViewCreated(view, savedInstanceState);\n' + logic)
        
        with open(filepath, 'w') as f:
            f.write(content)

