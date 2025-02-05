.globl	f1

f1:
	pushq	%rbp
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
	movq	$1, %rax
	movq	%rax, %rdx
	movq	$1, %rax
	addq	%rdx, %rax
	movq	$2, %rax
	movq	%rax, %rdx
	movq	$2, %rax
	subq	%rdx, %rax
	movq	$1, %rax
	movq %rax, %rdi
	call f1
	movq	$1, %rax
	movq	%rbp, %rsp
	popq	%rbp
	ret

