import os

filepath = 'app/src/main/java/com/example/midtermproject/ui/auth/AuthActivity.java'
with open(filepath, 'r') as f:
    content = f.read()

# Remove references to ivLogo, tvAppName, tvSubtitle in padding logic
content = content.replace("""            // Add top margin to logo
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) binding.ivLogo.getLayoutParams();
            mlp.topMargin = insets.top + (int)(40 * getResources().getDisplayMetrics().density);
            binding.ivLogo.setLayoutParams(mlp);""", "")

# Remove playEntryAnimations body or rewrite it just for cardForm
new_play_animations = """    private void playEntryAnimations() {
        // Fade in background slightly
        binding.ivLoginBg.setAlpha(0f);
        binding.ivLoginBg.animate()
                .alpha(1f)
                .setDuration(800)
                .start();

        // Slide up card
        binding.cardForm.setAlpha(0f);
        binding.cardForm.setTranslationY(100);
        binding.cardForm.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(200)
                .start();
    }"""
import re
content = re.sub(r'    private void playEntryAnimations\(\) \{[\s\S]*?\}\n', new_play_animations + '\n', content)

# Add switchToRegisterTab
switch_tab = """    public void switchToRegisterTab() {
        binding.viewPager.setCurrentItem(1, true);
    }"""
content = content.replace('    public void navigateToMain() {', switch_tab + '\n\n    public void navigateToMain() {')

with open(filepath, 'w') as f:
    f.write(content)

