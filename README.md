# PapersPleaseFaceGenerator
Generates a face based in the Papers Please universe. Don't kill me Dukope; it's just for fun.<br>To play around with it (since I was too lazy to make a Makefile, also it's trivial):

javac \*.java<br>
java FaceDisplayer

Press space to iterate through faces, created randomly based off of Lucas Dukope's templates. Pressing Esc or closing the window closes the window (duh).

The first row is where certain features are derived (shoulders, a template face, eyes, and nose & mouth). The second and third rows show the decompilation process and end with a compilation of the 4 body parts. The fourth and fifth rows are an attempt (albeit currently failed) at scaling these facial features to make it more natural and true to the original game.

Glaring issues: When I created this, I'd thought it would be some easy masking of certain facial features, then stitching them back together. However, upon inspection of the Dev log for Papers Please, I realized that the faces are scaled as well. I attempted to rectify my program but I can't seem to figure out the internal mechanic behind it.
