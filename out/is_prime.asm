.data



.text


start:
jal   main
li  $v0, 10
syscall


is_prime:
move  $fp, $sp
add  $sp, $sp, -80
sw  $fp, 4($sp)
sw  $ra, 8($sp)
sw  $t0, 12($sp)
sw  $t1, 16($sp)
sw  $t2, 20($sp)
sw  $t3, 24($sp)
sw  $t4, 28($sp)
sw  $t5, 32($sp)
sw  $t6, 36($sp)
sw  $t7, 40($sp)
sw  $s0, 44($sp)
sw  $s1, 48($sp)
sw  $s2, 52($sp)
sw  $s3, 56($sp)
sw  $s4, 60($sp)
sw  $s5, 64($sp)
sw  $s6, 68($sp)
sw  $s7, 72($sp)
sw  $t8, 76($sp)
sw  $t9, 80($sp)
addiu  $fp, $sp, 80
add  $sp, $sp, -4
add  $sp, $sp, -4
lw  $t9, -80($fp)
li  $t8, 2
sw  $t8, -80($fp)
lw  $t8, -84($fp)
li  $t9, 1
sw  $t9, -84($fp)
check1:
lw  $t9, -80($fp)
lw  $t8, 4($fp)
blt  $t9, $t8, Lt0
li  $s7, 0
b  Le0
Lt0:
li  $s7, 1
Le0:
beqz  $s7, label1
lw  $t8, 4($fp)
lw  $t9, -80($fp)
rem $s6, $t8, $t9
li  $t9, 0
beq  $s6, $t9, Lt1
li  $t8, 0
b  Le1
Lt1:
li  $t8, 1
Le1:
beqz  $t8, next1
lw  $t9, -84($fp)
li  $s6, 0
sw  $s6, -84($fp)
next1:

lw  $t8, -80($fp)
lw  $s6, -80($fp)
li  $t9, 1
add $s5, $s6, $t9
sw  $s5, -80($fp)
j check1
label1:
lw  $s7, -84($fp)
add  $v0, $zero, $s7
add  $sp, $sp, 8

lw  $fp, 4($sp)
lw  $ra, 8($sp)
lw  $t0, 12($sp)
lw  $t1, 16($sp)
lw  $t2, 20($sp)
lw  $t3, 24($sp)
lw  $t4, 28($sp)
lw  $t5, 32($sp)
lw  $t6, 36($sp)
lw  $t7, 40($sp)
lw  $s0, 44($sp)
lw  $s1, 48($sp)
lw  $s2, 52($sp)
lw  $s3, 56($sp)
lw  $s4, 60($sp)
lw  $s5, 64($sp)
lw  $s6, 68($sp)
lw  $s7, 72($sp)
lw  $t8, 76($sp)
lw  $t9, 80($sp)
add  $sp, $sp, 80
jr  $ra
main:
move  $fp, $sp
add  $sp, $sp, -80
sw  $fp, 4($sp)
sw  $ra, 8($sp)
sw  $t0, 12($sp)
sw  $t1, 16($sp)
sw  $t2, 20($sp)
sw  $t3, 24($sp)
sw  $t4, 28($sp)
sw  $t5, 32($sp)
sw  $t6, 36($sp)
sw  $t7, 40($sp)
sw  $s0, 44($sp)
sw  $s1, 48($sp)
sw  $s2, 52($sp)
sw  $s3, 56($sp)
sw  $s4, 60($sp)
sw  $s5, 64($sp)
sw  $s6, 68($sp)
sw  $s7, 72($sp)
sw  $t8, 76($sp)
sw  $t9, 80($sp)
addiu  $fp, $sp, 80
add  $sp, $sp, -4
lw  $s7, -80($fp)
li $v0, 5
syscall

sw  $v0, -80($fp)
lw  $s7, -80($fp)
la $a0, ($s7)
li $v0, 1
syscall

.data
str0:    .asciiz  " is "
.text
la  $t8, str0
la  $a0, ($t8)
li $v0, 4
syscall

add  $sp, $sp, -4	## space for return value
add  $sp, $sp, -4	# decrement stack for args
lw  $t9, -80($fp)
sw  $t9, 4($sp)
jal  is_prime
sw  $v0, -84($fp)
add $sp, $sp, 4	# restore stack from args
add  $sp, $sp, 4
add  $t8, $zero, $v0
li  $t9, 0
beq  $t8, $t9, Lt2
li  $s6, 0
b  Le2
Lt2:
li  $s6, 1
Le2:
beqz  $s6, next2
.data
str1:    .asciiz  "not "
.text
la  $t8, str1
la  $a0, ($t8)
li $v0, 4
syscall

next2:

.data
str2:    .asciiz  "prime.\n"
.text
la  $t8, str2
la  $a0, ($t8)
li $v0, 4
syscall

add  $sp, $sp, 4

lw  $fp, 4($sp)
lw  $ra, 8($sp)
lw  $t0, 12($sp)
lw  $t1, 16($sp)
lw  $t2, 20($sp)
lw  $t3, 24($sp)
lw  $t4, 28($sp)
lw  $t5, 32($sp)
lw  $t6, 36($sp)
lw  $t7, 40($sp)
lw  $s0, 44($sp)
lw  $s1, 48($sp)
lw  $s2, 52($sp)
lw  $s3, 56($sp)
lw  $s4, 60($sp)
lw  $s5, 64($sp)
lw  $s6, 68($sp)
lw  $s7, 72($sp)
lw  $t8, 76($sp)
lw  $t9, 80($sp)
add  $sp, $sp, 80
jr  $ra
