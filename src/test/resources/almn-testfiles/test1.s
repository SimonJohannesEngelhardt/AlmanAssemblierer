
	.globl	f1
	.type	f1, @function
f1:
	endbr64
	pushq	%rbp
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
	movq	$1, %rax
	movq %rax, %rdi
	call f1
	movq	$1, %rax
	movq	%rax, %rbx
	movq	$1, %rax
	addq	%rbx, %rax
	movq	$2, %rax
	movq	%rax, %rbx
	movq	$2, %rax
	subq	%rbx, %rax
	movq	$1, %rax
	movq	%rbp, %rsp
	popq	%rbp
	ret

	.globl	f2
	.type	f2, @function
f2:
	endbr64
	pushq	%rbp
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
	movq	$1, %rax
	movq %rax, %rdi
	call f1
	movq	$1, %rax
	movq	%rax, %rbx
	movq	$1, %rax
	addq	%rbx, %rax
	movq	$2, %rax
	movq	%rax, %rbx
	movq	$2, %rax
	subq	%rbx, %rax
	movq	$1, %rax
	movq	%rbp, %rsp
	popq	%rbp
	ret

