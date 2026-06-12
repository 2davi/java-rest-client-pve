
const { ref } = Vue;

/**
 * 
 * @param {() => object} createForm - Form 기본값을 새 객체로 찍어내는 팩토리 함수. (중첩 객체까지 새 인스턴스로)
 * 
 * @returns {{ form, isOpen, open, close }}
 */
function useModalForm(createForm) {
	//사용자에게 제공되는 사본 Form 객체
	const form = ref(createForm());
	const isOpen = ref(false);
	
	const open = (overrides = {}) => {
		form.value = {...createForm(), ...overrides};
		isOpen.value = true;
	};
	
	const close = () => {
		form.value = createForm();
		isOpen.value =false;
	};
	
	return {form, isOpen, open, close };
}