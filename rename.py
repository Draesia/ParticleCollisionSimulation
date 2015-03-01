import os
files = os.listdir('.')
index = 0
for filename in files:
    os.rename(filename, 'Image '+str(index+1)+'.png')
    index += 1